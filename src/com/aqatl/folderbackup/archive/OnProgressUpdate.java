package com.aqatl.folderbackup.archive;

public interface OnProgressUpdate {
	void progressUpdated(long completed, long total);
}
