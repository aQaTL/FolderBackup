package com.aqatl.folderbackup.archive;

/**
 * @author Maciej on 03.02.2018.
 */
public enum CompressionLevel {
	DEFAULT(-1, "Default"),
	NO_COMPRESSION(0, "No compression"),
	FASTEST(1, "Fastest"),
	NORMAL(5, "Normal"),
	MAXIMUM(7, "Maximum"),
	ULTRA(9, "Ultra");

	public final int level;
	public final String name;

	CompressionLevel(int level, String name) {
		this.level = level;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
