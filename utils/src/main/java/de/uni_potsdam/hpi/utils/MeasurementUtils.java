package de.uni_potsdam.hpi.utils;

public class MeasurementUtils {

	public static long sizeOf32(String s) {
		long bytes = 40 + 2 * s.length();
		
		bytes = (long)(8 * (Math.ceil(bytes / 8)));
			
		return bytes;
	}
	
	public static long sizeOf64(String s) {
		long bytes = 64 + 2 * s.length();
		
		bytes = (long)(8 * (Math.ceil(bytes / 8)));
		
		return bytes;
	}
}
