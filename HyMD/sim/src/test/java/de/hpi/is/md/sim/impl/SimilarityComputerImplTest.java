package de.hpi.is.md.sim.impl;

import de.hpi.is.md.sim.PairGenerator;
import de.hpi.is.md.sim.SimilarityComputerTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SimilarityComputerImplTest extends SimilarityComputerTest {

	@Parameter
	public boolean parallel;

	@Parameters
	public static Collection<Boolean> data() {
		return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
	}

	@Override
	protected <T> SimilarityComputerImpl<T> createComputer(PairGenerator<T> generator) {
		return SimilarityComputerImpl.<T>builder()
			.generator(generator)
			.parallel(parallel)
			.build();
	}

}