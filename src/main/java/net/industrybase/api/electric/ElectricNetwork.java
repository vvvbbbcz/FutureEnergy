package net.industrybase.api.electric;

import com.google.common.collect.*;
import net.industrybase.api.CapabilityList;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.server.RemoveWiresPacket;
import net.industrybase.api.network.server.WireConnSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * 电力网络管理类
 * 负责管理电力系统中的所有组件连接、能量传输和网络拓扑结构
 * 
 * <p>该类实现了以下核心功能：</p>
 * <ul>
 *   <li>电力组件的连接和断开管理</li>
 *   <li>电力网络的拓扑结构维护</li>
 *   <li>能量在网络中的分配和传输</li>
 *   <li>FE（Forge Energy）与内部电力系统的转换</li>
 *   <li>线缆连接的可视化同步</li>
 * </ul>
 * 
 * @author IndustryBase API
 * @since 1.0.0
 */
public class ElectricNetwork {
	private final Random random;
	private final HashMap<BlockPos, LinkedHashSet<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> sideConn;
	private final HashMultimap<BlockPos, BlockPos> wireConn;
	private final LevelAccessor level;
	private final ArrayDeque<Runnable> tasks;
	private final EnergyMap totalEnergy;
	private final HashMultiset<BlockPos> FEEnergy;
	private final HashMultiset<BlockPos> FEInput;
	private final EnergyMap machineEnergy;
	private final SetMultimap<BlockPos, Direction> FEMachines;
	private final HashMultimap<BlockPos, ServerPlayer> subscribes;

	/**
	 * 构造一个新的电力网络实例
	 * 
	 * @param level 关联的世界/维度访问器
	 */
	public ElectricNetwork(LevelAccessor level) {
		this.random = new Random();
		this.components = new HashMap<>();
		this.sideConn = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.wireConn = HashMultimap.create();
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.totalEnergy = new EnergyMap();
		this.FEEnergy = HashMultiset.create();
		this.FEInput = HashMultiset.create();
		this.machineEnergy = new EnergyMap();
		this.FEMachines = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.subscribes = HashMultimap.create();
	}

	/**
	 * 获取指定位置所在电力网络的组件数量
	 * 
	 * @param pos 要查询的方块位置
	 * @return 网络中的组件数量，如果不在网络中则返回1
	 */
	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	/**
	 * 获取指定位置所在电力网络的根节点位置
	 * 
	 * @param pos 要查询的方块位置
	 * @return 网络的根节点位置，如果不在网络中则返回自身位置
	 */
	public BlockPos root(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).getFirst() : pos;
	}

	/**
	 * 获取指定位置所在电力网络的总输出功率
	 * 
	 * @param pos 要查询的方块位置
	 * @return 网络的总输出功率（单位：EP/t）
	 */
	public double getTotalOutput(BlockPos pos) {
		return this.totalEnergy.get(this.root(pos)).getOutput();
	}

	/**
	 * 获取指定位置所在电力网络的总输入需求
	 * 
	 * @param pos 要查询的方块位置
	 * @return 网络的总输入需求（单位：EP/t）
	 */
	public double getTotalInput(BlockPos pos) {
		return this.totalEnergy.get(this.root(pos)).getInput();
	}

	/**
	 * 获取指定位置的所有线缆连接
	 * 
	 * @param pos 要查询的方块位置
	 * @return 与该位置连接的所有线缆端点位置集合
	 */
	public Set<BlockPos> getWireConn(BlockPos pos) {
		return this.wireConn.get(pos);
	}

	/**
	 * 订阅指定位置的线缆连接更新
	 * 
	 * @param pos 要订阅的方块位置
	 * @param player 订阅的玩家
	 * @return 当前的线缆连接集合
	 */
	public Set<BlockPos> subscribeWire(BlockPos pos, ServerPlayer player) {
		this.subscribes.put(pos, player);
		return this.wireConn.get(pos);
	}

	/**
	 * 取消订阅指定位置的线缆连接更新
	 * 
	 * @param pos 要取消订阅的方块位置
	 * @param player 取消订阅的玩家
	 */
	public void unsubscribeWire(BlockPos pos, ServerPlayer player) {
		this.subscribes.remove(pos, player);
	}

	/**
	 * 在客户端添加线缆连接（仅用于渲染）
	 * 
	 * @param from 起始位置
	 * @param to 目标位置
	 */
	public void addClientWire(BlockPos from, BlockPos to) {
		if (this.level.isClientSide()) {
			this.wireConn.put(from, to);
		}
	}

	/**
	 * 在客户端批量添加线缆连接（仅用于渲染）
	 * 
	 * @param pos 起始位置
	 * @param data 目标位置集合
	 */
	public void addClientWire(BlockPos pos, Collection<BlockPos> data) {
		if (this.level.isClientSide()) {
			this.wireConn.putAll(pos, data);
		}
	}

	/**
	 * 在客户端移除线缆连接（仅用于渲染）
	 * 
	 * @param from 起始位置
	 * @param to 目标位置
	 */
	public void removeClientWire(BlockPos from, BlockPos to) {
		if (this.level.isClientSide()) {
			this.wireConn.remove(from, to);
		}
	}

	/**
	 * 在客户端移除指定位置的所有线缆连接（仅用于渲染）
	 * 
	 * @param from 要移除连接的位置
	 */
	public void removeClientWires(BlockPos from) {
		if (this.level.isClientSide()) {
			this.wireConn.get(from).forEach(to -> this.wireConn.remove(to, from));
			this.wireConn.removeAll(from);
		}
	}

	/**
	 * 获取指定位置机器的输出功率
	 * 
	 * @param pos 机器位置
	 * @return 机器的输出功率（单位：EP/t）
	 */
	public double getMachineOutput(BlockPos pos) {
		return this.machineEnergy.get(pos).getOutput();
	}
	
	/**
	 * 设置指定位置机器的输出功率
	 * 
	 * @param pos 机器位置
	 * @param power 要设置的输出功率（单位：EP/t）
	 * @return 功率变化量
	 */
	public double setMachineOutput(BlockPos pos, double power) {
		long powerLong = (long) Math.max(power * 100.0D, 0.0D);
		long outputOld = this.machineEnergy.get(pos).getOutputLong();
		if (outputOld == powerLong) return 0.0D;

		long diff = powerLong - outputOld;
		this.machineEnergy.addOutput(pos, diff);
		this.totalEnergy.addOutput(this.root(pos), diff);
		return diff / 100.0D;
	}

	/**
	 * 获取指定位置机器的输入需求
	 * 
	 * @param pos 机器位置
	 * @return 机器的输入需求（单位：EP/t）
	 */
	public double getMachineInput(BlockPos pos) {
		return this.machineEnergy.get(pos).getInput();
	}

	/**
	 * 设置指定位置机器的输入需求
	 * 
	 * @param pos 机器位置
	 * @param power 要设置的输入需求（单位：EP/t）
	 * @return 需求变化量
	 */
	public double setMachineInput(BlockPos pos, double power) {
		long powerLong = (long) Math.max(power * 100.0D, 0.0D);
		long inputOld = this.machineEnergy.get(pos).getInputLong();
		if (inputOld == powerLong) return 0.0D;

		long diff = powerLong - inputOld;
		this.machineEnergy.addInput(pos, diff);
		this.totalEnergy.addInput(this.root(pos), diff);
		return diff / 100.0D;
	}

	/**
	 * 获取指定位置机器的实际输入功率
	 * 根据网络的供需平衡计算实际能够获得的功率
	 * 
	 * @param pos 机器位置
	 * @return 实际输入功率（单位：EP/t）
	 */
	public double getRealInput(BlockPos pos) {
		BlockPos root = this.root(pos);
		EnergyMap.Energy energy = this.totalEnergy.get(root);
		long totalOutput = energy.getOutputLong() + (this.FEInput.count(root) * 100L);
		long totalInput = energy.getInputLong();
		double machineInput = this.machineEnergy.get(pos).getInput();
		if (totalInput > 0L) {
			if (totalOutput >= totalInput) {
				return machineInput;
			} else {
				return machineInput * totalOutput / totalInput;
			}
		}
		return 0.0D;
	}

	/**
	 * 获取指定位置网络中存储的FE能量
	 * 
	 * @param pos 查询位置
	 * @return 存储的FE能量数量
	 */
	public int getFEEnergy(BlockPos pos) {
		return this.FEEnergy.count(this.root(pos));
	}

	/**
	 * 获取指定位置网络的最大FE存储容量
	 * 
	 * @param pos 查询位置
	 * @return 最大FE存储容量
	 */
	public int getMaxFEStored(BlockPos pos) {
		return 100 * this.size(pos);
	}

	/**
	 * 向指定位置的网络接收FE能量
	 * 
	 * @param pos 接收位置
	 * @param maxReceive 最大接收量
	 * @param simulate 是否为模拟模式（不实际接收）
	 * @return 实际接收的FE能量数量
	 */
	public int receiveFEEnergy(BlockPos pos, int maxReceive, boolean simulate) {
		BlockPos root = this.root(pos);
		int receive = Math.min(maxReceive, this.getMaxFEStored(root) - this.FEEnergy.count(root));
		if (!simulate) this.FEEnergy.add(root, receive);
		return receive;
	}

	/**
	 * 从指定位置的网络提取FE能量
	 * 
	 * @param pos 提取位置
	 * @param maxExtract 最大提取量
	 * @param simulate 是否为模拟模式（不实际提取）
	 * @return 实际提取的FE能量数量
	 */
	public int extractFEEnergy(BlockPos pos, int maxExtract, boolean simulate) {
		BlockPos root = this.root(pos);
		int extract = Math.min(maxExtract, this.FEEnergy.count(root));
		if (!simulate) this.FEEnergy.remove(root, extract);
		return extract;
	}

	/**
	 * 从电力网络中移除指定方块
	 * 该操作会断开所有相关连接并重新计算网络拓扑
	 * 
	 * @param pos 要移除的方块位置
	 * @param callback 移除完成后的回调函数
	 */
	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.totalEnergy.shrink(this.root(pos), this.machineEnergy.remove(pos));
			for (Direction side : Direction.values()) {
				this.cutSide(pos, side);
			}
			this.FEMachines.removeAll(pos); // 移除相应的 FE 机器
			if (!this.wireConn.get(pos).isEmpty()) {
				Iterator<BlockPos> iterator = this.wireConn.get(pos).iterator();
				while (iterator.hasNext()) {
					BlockPos another = iterator.next();
					iterator.remove();
					this.wireConn.remove(another, pos);
					this.spilt(pos, another);
					if (this.level.isAreaLoaded(another, 0)) {
						this.level.getChunk(another).setUnsaved(true);
					}
				}
				this.subscribes.get(pos).forEach(player -> PacketDistributor.sendToPlayer(player, new RemoveWiresPacket(pos)));
				this.subscribes.removeAll(pos);
			}
			callback.run();
		});
	}

	/**
	 * 移除两个位置之间的线缆连接
	 * 
	 * @param from 起始位置
	 * @param to 目标位置
	 */
	public void removeWire(BlockPos from, BlockPos to) {
		this.tasks.offer(() -> {
			this.cutWire(from, to);
			this.level.getChunk(from).setUnsaved(true); // 不能检查区块是否加载
			this.level.getChunk(to).setUnsaved(true);
		});
	}

	private void cutSide(BlockPos node, Direction direction) {
		if (this.sideConn.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.sideConn.remove(another, direction.getOpposite());
			this.spilt(node, another);
		}
	}

	/**
	 * 切断两个位置之间的线缆连接并同步给客户端
	 * 
	 * @param from 起始位置
	 * @param to 目标位置
	 */
	public void cutWire(BlockPos from, BlockPos to) {
		if (this.wireConn.remove(from, to)) {
			this.wireConn.remove(to, from);
			this.spilt(from, to);
			this.subscribes.get(from).forEach(player ->
					PacketDistributor.sendToPlayer(player, new WireConnSyncPacket(from, to, true)));
			this.subscribes.get(to).forEach(player ->
					PacketDistributor.sendToPlayer(player, new WireConnSyncPacket(to, from, true)));
		}
	}

	/**
	 * 分割电力网络
	 * 当连接断开时，检查网络是否需要分割成两个独立的网络
	 * 
	 * @param node 第一个节点
	 * @param another 第二个节点
	 */
	private void spilt(BlockPos node, BlockPos another) {
		BFSIterator nodeIterator = new BFSIterator(node);
		BFSIterator anotherIterator = new BFSIterator(another);

		while (nodeIterator.hasNext()) {
			BlockPos next = nodeIterator.next();
			if (!anotherIterator.getSearched().contains(next)) {
				BFSIterator iterator = anotherIterator;
				anotherIterator = nodeIterator;
				nodeIterator = iterator;
				continue;
			}
			return;
		}

		LinkedHashSet<BlockPos> primaryComponent = this.components.get(node);
		LinkedHashSet<BlockPos> secondaryComponent;
		BlockPos primaryNode = primaryComponent.getFirst();
		LinkedHashSet<BlockPos> searched = nodeIterator.getSearched();

		if (searched.contains(primaryNode)) {
			secondaryComponent = new LinkedHashSet<>(Sets.difference(primaryComponent, searched));
			primaryComponent.retainAll(searched);
		} else {
			secondaryComponent = searched;
			primaryComponent.removeAll(searched);
		}

		BlockPos secondaryNode = secondaryComponent.getFirst();
		if (secondaryComponent.size() <= 1) {
			this.components.remove(secondaryNode);

			EnergyMap.Energy diff = this.machineEnergy.get(secondaryNode);
			this.totalEnergy.shrink(primaryNode, diff);
		} else {
			EnergyMap.TempEnergy diff = new EnergyMap.TempEnergy();
			for (BlockPos pos : secondaryComponent) {
				this.components.put(pos, secondaryComponent);
				diff.add(this.machineEnergy.get(pos));
			}
			this.totalEnergy.shrink(primaryNode, diff);
			this.totalEnergy.put(secondaryNode, diff);
		}
		if (primaryComponent.size() <= 1) {
			this.components.remove(primaryNode);
			// 已在 shrink 中完成对 primaryNode 的能量的检查和清除
		}
		// 分配 FE 能量
		int primarySize = this.size(primaryNode), secondarySize = this.size(secondaryNode);
		int diff = this.FEEnergy.count(primaryNode) * secondarySize / (primarySize + secondarySize);
		this.FEEnergy.remove(primaryNode, diff);
		this.FEEnergy.add(secondaryNode, diff);
	}

	/**
	 * 添加或更新电力网络中的方块
	 * 检查周围连接并更新网络拓扑结构
	 * 
	 * @param pos 方块位置
	 * @param callback 操作完成后的回调函数
	 */
	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				if (this.hasElectricalCapability(pos, side)) {
					if (this.hasElectricalCapability(pos.relative(side), side.getOpposite())) {
						this.FEMachines.remove(pos.immutable(), side);
						this.linkSide(pos, side);
					} else if (this.hasFECapability(pos.relative(side), side.getOpposite())) {
						this.FEMachines.put(pos.immutable(), side);
						this.cutSide(pos, side);
					} else {
						this.FEMachines.remove(pos.immutable(), side);
						this.cutSide(pos, side);
					}
				} else {
					this.FEMachines.remove(pos.immutable(), side);
					this.cutSide(pos, side);
				}
			}
			callback.run();
		});
	}

	/**
	 * 在两个位置之间添加线缆连接
	 * 
	 * @param from 起始位置
	 * @param to 目标位置
	 * @param callback 添加完成后的回调函数
	 * @return 如果成功添加连接则返回true，否则返回false
	 */
	public boolean addWire(BlockPos from, BlockPos to, Runnable callback) {
		if (!from.equals(to) && !this.wireConn.containsEntry(from, to)) {
			return this.tasks.offer(() -> {
				linkWire(from, to);
				callback.run(); // 只需要一方保存即可，不影响最终加载后的连通域
			});
		}
		return false;
	}

	/**
	 * 检查指定位置和方向是否具有电力能力
	 * 
	 * @param pos 方块位置
	 * @param side 检查的方向
	 * @return 如果具有电力能力则返回true
	 */
	private boolean hasElectricalCapability(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			if (blockEntity != null) {
				Level level = blockEntity.getLevel();
				if (level != null) {
					return level.getCapability(CapabilityList.ELECTRIC_POWER, pos, null, blockEntity, side) != null;
				}
			}
		}
		return false;
	}

	/**
	 * 检查指定位置和方向是否具有FE（Forge Energy）能力
	 * 
	 * @param pos 方块位置
	 * @param side 检查的方向
	 * @return 如果具有FE能力则返回true
	 */
	private boolean hasFECapability(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			if (blockEntity != null) {
				Level level = blockEntity.getLevel();
				if (level != null) {
					return level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null, blockEntity, side) != null;
				}
			}
		}
		return false;
	}

	private void linkSide(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		if (this.sideConn.put(secondary, direction)) {
			BlockPos primary = secondary.relative(direction);
			this.sideConn.put(primary, direction.getOpposite());
			this.link(primary, secondary);
		}
	}

	private void linkWire(BlockPos from, BlockPos to) {
		BlockPos secondary = from.immutable();
		BlockPos primary = to.immutable();
		if (this.wireConn.put(secondary, primary)) {
			this.wireConn.put(primary, secondary);
			this.link(primary, secondary);
			this.subscribes.get(from).forEach(player ->
					PacketDistributor.sendToPlayer(player, new WireConnSyncPacket(from, to, false)));
			this.subscribes.get(to).forEach(player ->
					PacketDistributor.sendToPlayer(player, new WireConnSyncPacket(to, from, false)));
		}
	}

	/**
	 * 连接两个电力网络节点
	 * 合并两个节点所在的网络，统一管理能量分配
	 * 
	 * @param primary 主节点位置
	 * @param secondary 次节点位置
	 */
	private void link(BlockPos primary, BlockPos secondary) {
		LinkedHashSet<BlockPos> primaryComponent = this.components.get(primary);
		LinkedHashSet<BlockPos> secondaryComponent = this.components.get(secondary);

		EnergyMap.Energy primaryEnergy = this.machineEnergy.get(primary);
		EnergyMap.Energy secondaryEnergy = this.machineEnergy.get(secondary);

		if (primaryComponent == null && secondaryComponent == null) {
			LinkedHashSet<BlockPos> union = new LinkedHashSet<>();
			this.components.put(secondary, union);
			this.components.put(primary, union);
			union.add(secondary);
			union.add(primary);

			this.totalEnergy.put(secondary, EnergyMap.Energy.union(primaryEnergy, secondaryEnergy));
			this.totalEnergy.remove(primary);
			this.mergeFE(secondary, primary);
		} else if (primaryComponent == null) {
			BlockPos secondaryNode = secondaryComponent.getFirst();
			this.components.put(primary, secondaryComponent);
			secondaryComponent.add(primary);

			this.totalEnergy.add(secondaryNode, primaryEnergy);
			this.totalEnergy.remove(primary);
			this.mergeFE(secondaryNode, primary);
		} else if (secondaryComponent == null) {
			BlockPos primaryNode = primaryComponent.getFirst();
			this.components.put(secondary, primaryComponent);
			primaryComponent.add(secondary);

			this.totalEnergy.add(primaryNode, secondaryEnergy);
			this.totalEnergy.remove(secondary);
			this.mergeFE(primaryNode, secondary);
		} else if (primaryComponent != secondaryComponent) {
			BlockPos primaryNode = primaryComponent.getFirst();
			BlockPos secondaryNode = secondaryComponent.getFirst();
			LinkedHashSet<BlockPos> union = new LinkedHashSet<>(Sets.union(primaryComponent, secondaryComponent));
			union.forEach(pos -> this.components.put(pos, union));

			this.totalEnergy.add(primaryNode, this.totalEnergy.remove(secondaryNode));
			this.mergeFE(primaryNode, secondaryNode);
		}
	}

	/**
	 * 合并两个网络节点的FE能量
	 * 
	 * @param primaryNode 主节点位置
	 * @param secondaryNode 次节点位置
	 */
	private void mergeFE(BlockPos primaryNode, BlockPos secondaryNode) {
		int diff = this.FEEnergy.count(secondaryNode);
		this.FEEnergy.remove(secondaryNode, diff);
		this.FEEnergy.add(primaryNode, diff);
	}

	/**
	 * 网络tick开始时的处理
	 * 执行所有待处理的任务队列
	 */
	private void tickStart() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	/**
	 * 网络tick结束时的处理
	 * 处理FE能量的输入输出和EP与FE之间的转换
	 */
	private void tickEnd() {
		HashSet<BlockPos> updated = new HashSet<>();
		Multiset<BlockPos> forgeEnergy = HashMultiset.create();
		for (Map.Entry<BlockPos, Direction> entry : this.shuffle(this.FEMachines.entries())) {
			BlockPos pos = entry.getKey();
			Direction direction = entry.getValue();
			BlockPos target = pos.relative(direction);
			if (this.level.isAreaLoaded(target, 0)) {
				BlockPos root = this.root(pos);
				// 将剩余 EP 转换为 FE
				if (updated.add(root)) { // 已转换过的能量网络则跳过
					EnergyMap.Energy energy = this.totalEnergy.get(root);
					long power = energy.getOutputLong();
					long excess = power - energy.getInputLong();
					if (excess > 0L) {
						forgeEnergy.add(root, (int) Math.floor(excess / 100.0D));
					}
				}

				// 向 FE 方块输出能量
				BlockEntity blockEntity = this.level.getBlockEntity(target);
				if (blockEntity != null) {
					Level level = blockEntity.getLevel();
					if (level != null) {
						IEnergyStorage capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, target, null, blockEntity, direction.getOpposite());
						if (capability != null) {
							if (capability.canReceive()) {
								int diff = forgeEnergy.count(root);
								int FEDiff = this.FEEnergy.count(root);
								forgeEnergy.remove(root, capability.receiveEnergy(diff, false));
								if (forgeEnergy.count(root) <= 0) { // 先将 EP 转化的 FE 分配完
									this.FEEnergy.remove(root, capability.receiveEnergy(FEDiff, false));
								}
							}
						}
					}
				}
			}
		}
		// 将未使用 FE 转化为 EP
		this.FEInput.clear(); // 清除先前转换的 EP
		this.FEEnergy.forEachEntry((root, count) -> {
			EnergyMap.Energy energy = this.totalEnergy.get(root);
			long lack = energy.getInputLong() - energy.getOutputLong();
			if (lack > 0L) {
				this.FEInput.setCount(root, Math.min((int) Math.ceil(lack / 100.0D), count));
			}
		});
		this.FEInput.forEachEntry(this.FEEnergy::remove);
	}

	/**
	 * 随机打乱集合元素顺序
	 * 用于确保能量分配的公平性
	 * 
	 * @param <T> 元素类型
	 * @param iterable 要打乱的集合
	 * @return 打乱后的列表
	 */
	private <T> List<T> shuffle(Collection<? extends T> iterable) {
		List<T> list = new ArrayList<>(iterable);
		Collections.shuffle(list, this.random);
		return list;
	}

	/**
	 * 广度优先搜索迭代器
	 * 用于遍历电力网络中的连通组件
	 */
	public class BFSIterator implements Iterator<BlockPos> {
		private final LinkedHashSet<BlockPos> searched = new LinkedHashSet<>();
		private final ArrayDeque<BlockPos> queue = new ArrayDeque<>();

		/**
		 * 构造BFS迭代器
		 * 
		 * @param node 起始节点位置
		 */
		public BFSIterator(BlockPos node) {
			node = node.immutable();
			this.searched.add(node);
			this.queue.offer(node);
		}

		/**
		 * 检查是否还有下一个节点
		 * 
		 * @return 如果还有未访问的节点则返回true
		 */
		public boolean hasNext() {
			return !this.queue.isEmpty();
		}

		/**
		 * 获取下一个节点并扩展搜索
		 * 
		 * @return 下一个节点位置
		 */
		@Override
		public BlockPos next() {
			BlockPos node = this.queue.remove();
			for (Direction direction : ElectricNetwork.this.sideConn.get(node)) {
				BlockPos another = node.relative(direction);
				if (this.searched.add(another)) {
					this.queue.offer(another);
				}
			}
			for (BlockPos another : ElectricNetwork.this.wireConn.get(node)) {
				if (this.searched.add(another)) {
					this.queue.offer(another);
				}
			}
			return node;
		}

		/**
		 * 获取已搜索过的节点集合
		 * 
		 * @return 已搜索的节点位置集合
		 */
		public LinkedHashSet<BlockPos> getSearched() {
			return this.searched;
		}
	}

	/**
	 * 电力网络管理器
	 * 负责管理所有世界/维度的电力网络实例，并处理相关事件
	 */
	@EventBusSubscriber(modid = IndustryBaseApi.MODID)
	public static class Manager {
		private static final Map<LevelAccessor, ElectricNetwork> INSTANCES = new IdentityHashMap<>();

		/**
		 * 获取指定世界/维度的电力网络实例
		 * 如果不存在则创建新实例
		 * 
		 * @param level 世界/维度访问器
		 * @return 对应的电力网络实例
		 * @throws NullPointerException 如果level为null
		 */
		public static ElectricNetwork get(LevelAccessor level) {
			return INSTANCES.computeIfAbsent(Objects.requireNonNull(level, "Level can't be null!"), ElectricNetwork::new);
		}

		/**
		 * 处理世界卸载事件
		 * 清理对应的电力网络实例以防止内存泄漏
		 * 
		 * @param event 世界卸载事件
		 */
		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			INSTANCES.remove(event.getLevel());
		}

		/**
		 * 处理世界tick开始事件
		 * 执行电力网络的tick开始逻辑
		 * 
		 * @param event 世界tick开始事件
		 */
		@SubscribeEvent
		public static void onLevelTick(LevelTickEvent.Pre event) {
			if (!event.getLevel().isClientSide) {
				get(event.getLevel()).tickStart();
			}
		}

		/**
		 * 处理世界tick结束事件
		 * 执行电力网络的tick结束逻辑，处理能量分配
		 * 
		 * @param event 世界tick结束事件
		 */
		@SubscribeEvent
		public static void onLevelTick(LevelTickEvent.Post event) {
			if (!event.getLevel().isClientSide) {
				get(event.getLevel()).tickEnd();
			}
		}
	}
}
