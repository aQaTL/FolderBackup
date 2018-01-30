package com.aqatl.folderbackup.archive;

import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.InputStreamSequentialInStream;
import net.sf.sevenzipjbinding.impl.OutItemFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Maciej on 29.01.2018.
 */
public class ArchiveItemCallback implements IOutCreateCallback<IOutItemAllFormats> {
	private List<File> files;
	private String basePath;

	private long total = 0;

	private OnOperationCompleted onOperationCompleted;
	private OnProgressUpdate onProgressUpdate;

	public ArchiveItemCallback(List<File> files, String basePath, OnOperationCompleted onOperationCompleted, OnProgressUpdate onProgressUpdate) throws IOException {
		this.files = files;
		this.basePath = basePath;
		this.onOperationCompleted = onOperationCompleted;
		this.onProgressUpdate = onProgressUpdate;
	}

	@Override
	public void setOperationResult(boolean operationResultOk) throws SevenZipException {
		onOperationCompleted.operationCompleted(operationResultOk);
	}

	@Override
	public IOutItemAllFormats getItemInformation(int index, OutItemFactory<IOutItemAllFormats> outItemFactory) throws SevenZipException {
		IOutItemAllFormats outItem = outItemFactory.createOutItem();
		File file = files.get(index);
		outItem.setPropertyIsDir(file.isDirectory());
		outItem.setDataSize(file.length());
		outItem.setPropertyPath(file.getPath().substring(basePath.length() + 1, file.getPath().length()));
		outItem.setPropertyCreationTime(new Date());
		return outItem;
	}

	@Override
	public ISequentialInStream getStream(int index) throws SevenZipException {
		try {
			return new InputStreamSequentialInStream(new FileInputStream(files.get(index)));
		}
		catch (FileNotFoundException e) {
			throw new SevenZipException(e);
		}
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

