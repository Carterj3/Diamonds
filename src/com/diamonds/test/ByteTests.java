package com.diamonds.test;

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.Test;

public class ByteTests {

	@Test
	public void convert_unconvert(){
		Assert.assertEquals(1, convertFromBytes(convertToBytes(1)));
	}
	
	public byte[] convertToBytes(int i){
		ByteBuffer b = ByteBuffer.allocate(4);
		
		b.putInt(i);

		return b.array();
	}
	
	public int convertFromBytes(byte[] b){
		return ByteBuffer.wrap(b).getInt();
	}
}
