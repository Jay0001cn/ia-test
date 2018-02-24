package config;

import static org.junit.Assert.*;

import org.junit.Test;

public class JobAttributeManagerTest {
	private static JobAttributeManager sut;
	
	public void init(){
		
		
		
	}
	
	@Test
	public void test() {
		def jobIdentifier = '4744890'
		
		sut = new JobAttributeManager(jobIdentifier:jobIdentifier)
		sut.testInProd();
		
		def serverAttrs = sut.loadServerAttributes()
		println serverAttrs
		
		def jobAttrs = sut.loadJobAttributes()
		println jobAttrs
	}

}
