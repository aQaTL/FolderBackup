package com.aqatl.folderbackup.archive;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Maciej on 29.01.2018.
 */
public class Archiver {

	private OnProgressUpdate onProgressUpdate;
	private CompressionLevel compressionLevel;

	public Archiver() throws SevenZipNativeInitializationException {
		SevenZip.initSevenZipFromPlatformJAR();
	}

	public List<ArchiveFormat> getSupportedFormats() {
		return Arrays.stream(ArchiveFormat.values()).
				filter(ArchiveFormat::isOutArchiveSupported).
				collect(Collectors.toList());
	}

	public boolean supportsFormat(ArchiveFormat format) {
		return getSupportedFormats().contains(format);
	}

	public boolean archive(File input, File output, ArchiveFormat outputFormat) throws IOException {
		AtomicBoolean archivedSuccessfully = new AtomicBoolean(false);

		IOutCreateArchive<IOutItemAllFormats> archive = SevenZip.openOutArchive(outputFormat);
		if (archive instanceof IOutFeatureSetLevel) {
			((IOutFeatureSetLevel) archive).setLevel(compressionLevel.level);
		}
		if (archive instanceof IOutFeatureSetMultithreading) {
			((IOutFeatureSetMultithreading) archive).setThreadCount(Runtime.getRuntime().availableProcessors());
		}
		RandomAccessFile outputFile = new RandomAccessFile(output, "rw");

		try {
			List<File> files = Files.walk(input.toPath()).map(Path::toFile).collect(Collectors.toList());

			archive.createArchive(
					new RandomAccessFileOutStream(outputFile),
					files.size(),
					new ArchiveItemCallback(
							files,
							input.getParent(),
							archivedSuccessfully::set,
							onProgressUpdate));
		}
		finally {
			archive.close();
			outputFile.close();
		}

		return archivedSuccessfully.get();
	}

	public OnProgressUpdate getOnProgressUpdate() {
		return onProgressUpdate;
	}

	public void setOnProgressUpdate(OnProgressUpdate onProgressUpdate) {
		this.onProgressUpdate = onProgressUpdate;
	}

	public CompressionLevel getCompressionLevel() {
		return compressionLevel;
	}

	public void setCompressionLevel(CompressionLevel compressionLevel) {
		this.compressionLevel = compressionLevel;
	}
}
