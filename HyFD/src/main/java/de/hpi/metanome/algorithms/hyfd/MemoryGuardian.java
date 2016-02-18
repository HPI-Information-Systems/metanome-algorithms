package de.hpi.metanome.algorithms.hyfd;

import java.lang.management.ManagementFactory;

import de.hpi.metanome.algorithms.hyfd.structures.FDTree;

public class MemoryGuardian {
	
	private boolean active;
	private final float maxMemoryUsagePercentage = 0.7f;	// Memory usage in percent from which a lattice level should be dropped
	private final int memoryCheckFrequency = 100000;		// Number of allocation events that cause a memory check
	private int allocationEventsSinceLastCheck = 0;
	private long maxMemoryUsage;
	private long availableMemory;

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public MemoryGuardian(boolean active) {
		this.active = active;
		this.availableMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		this.maxMemoryUsage = (long)(this.availableMemory * this.maxMemoryUsagePercentage);
	}
	
	public void memoryChanged(int allocationEvents) {
		this.allocationEventsSinceLastCheck += allocationEvents;
	}

	public void match(FDTree posCover) {
		if ((!this.active) || (this.allocationEventsSinceLastCheck < this.memoryCheckFrequency))
			return;
		
		System.gc();
		
		while (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() > this.maxMemoryUsage) {
			posCover.trim(posCover.getDepth() - 1);
			System.out.print(" (trim " + posCover.getMaxDepth() + ")");
			System.gc();
		}
		
		this.allocationEventsSinceLastCheck = 0;
	}
}
