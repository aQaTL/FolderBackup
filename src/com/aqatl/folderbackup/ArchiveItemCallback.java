package com.aqatl.folderbackup;

import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

/**
 * @author Maciej on 29.01.2018.
 */
public class ArchiveItemCallback implements IOutCreateCallback<IOutItemAllFormats> {
	private List<File> files;
	private String basePath;
	private byte[][] data;

	private long total = 0;

	private OnOperationCompleted onOperationCompleted;
	private OnProgressUpdate onProgressUpdate;

	public ArchiveItemCallback(List<File> files, String basePath, OnOperationCompleted onOperationCompleted, OnProgressUpdate onProgressUpdate) throws IOException {
		this.files = files;
		this.basePath = basePath;
		this.onOperationCompleted = onOperationCompleted;
		this.onProgressUpdate = onProgressUpdate;
		data = new byte[files.size()][];
	}

	@Override
	public void setOperationResult(boolean operationResultOk) throws SevenZipException {
		onOperationCompleted.operationCompleted(operationResultOk);
	}

	@Override
	public IOutItemAllFormats getItemInformation(int index, OutItemFactory<IOutItemAllFormats> outItemFactory) throws SevenZipException {
		IOutItemAllFormats outItem = outItemFactory.createOutItem();
		File file = files.get(index);
		if (file.isDirectory()) {
			outItem.setPropertyIsDir(true);
			outItem.setDataSize((long) 0);
			data[index] = new byte[]{};
		} else {
			try {
				data[index] = Files.readAllBytes(file.toPath());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		outItem.setDataSize((long) data[index].length);
		outItem.setPropertyPath(file.getPath().substring(basePath.length() + 1, file.getPath().length()));
		outItem.setPropertyCreationTime(new Date());
		return outItem;
	}

	@Override
	public ISequentialInStream getStream(int index) throws SevenZipException {
		return new ByteArrayStream(data[index], true);
	}

	@Override
	public void setTotal(long total) throws SevenZipException {
		this.total = total;
	}

	@Override
	public void setCompleted(long complete) throws SevenZipException {
		onProgressUpdate.progressUpdated(complete, total);
	}
}

interface OnOperationCompleted {
	void operationCompleted(boolean success);
}

interface OnProgressUpdate {
	void progressUpdated(long completed, long total);
}
