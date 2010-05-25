package org.jbei.ice.services.johnny5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.johnny5.vo.FileInfo;

public class Johnny5HelperService {
    public byte[] archiveJohnny5Files(String prefix, String partFile, String targetFile,
            String seqFile, List<FileInfo> fileList) {
        final int BUFFER = 4096;

        byte[] resultBytes = null;

        UsageLogger.info("Johnny5HelperService: archiving johnny5 files...");

        String source = prefix + "_seqListFile.csv";
        String source2 = prefix + "_partListFile.csv";
        String source3 = prefix + "_targetListFile.csv";
        String target = JbeirSettings.getSetting("DATA_DIRECTORY") + "/" + prefix
                + "_completeOutput-" + Utils.generateUUID() + ".zip";

        try {
            // Write first three arguments to a files
            try {
                // Create file
                FileWriter fstream = new FileWriter(source);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(seqFile);
                // Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            try {
                // Create file
                FileWriter fstream = new FileWriter(source2);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(partFile);
                // Close the output stream
                out.close();
            } catch (Exception e) {// Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            try {
                // Create file
                FileWriter fstream = new FileWriter(source3);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(targetFile);
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            for (FileInfo fileInfo : fileList) {
                try {
                    // Create file
                    FileWriter fstream = new FileWriter(fileInfo.getName());
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(fileInfo.getFile());
                    //Close the output stream
                    out.close();
                } catch (Exception e) {//Catch exception if any
                    System.err.println("Error: " + e.getMessage());
                }
            }

            try {

                FileOutputStream dest = new FileOutputStream(target);
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(dest));

                byte data[] = new byte[BUFFER];

                for (int i = 0; i < 3 + fileList.size(); i++) {
                    FileInputStream fis;
                    ZipEntry entry;
                    if (i == 0) {
                        fis = new FileInputStream(source);
                        entry = new ZipEntry(source);
                    } else if (i == 1) {
                        fis = new FileInputStream(source2);
                        entry = new ZipEntry(source2);
                    } else if (i == 2) {
                        fis = new FileInputStream(source3);
                        entry = new ZipEntry(source3);
                    } else {
                        FileInfo fi = fileList.get(i - 3);
                        fis = new FileInputStream(fi.getName());
                        entry = new ZipEntry(fi.getName());
                    }

                    BufferedInputStream origin = new BufferedInputStream(fis, BUFFER);
                    zos.putNextEntry(entry);

                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        zos.write(data, 0, count);
                    }

                    origin.close();

                }

                // Finish zip process
                zos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //clean up
            File del1 = new File(source);
            File del2 = new File(source2);
            File del3 = new File(source3);
            del1.delete();
            del2.delete();
            del3.delete();

            for (FileInfo fileInfo : fileList) {
                File del = new File(fileInfo.getName());
                del.delete();
            }

            // Reading bytes
            FileInputStream fis = new FileInputStream(target);
            FileChannel fc = fis.getChannel();

            resultBytes = new byte[(int) (fc.size())];
            ByteBuffer bb = ByteBuffer.wrap(resultBytes);
            fc.read(bb);
        } catch (Exception e) {
            UsageLogger.error(Utils.stackTraceToString(e));
        }

        UsageLogger.info("Johnny5HelperService: archived successfully. " + target);

        return resultBytes;
    }
}