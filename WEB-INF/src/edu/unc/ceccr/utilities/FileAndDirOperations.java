package edu.unc.ceccr.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
There is a lot of code strewn around the system for doing things like
copying files, copying directories, deleting directories, and so on. 
We need to put all of this in one place. Here's good.
*/

public class FileAndDirOperations {
	
	public static int countFilesInDirMatchingPattern(String dir, String pattern){
		int count = 0;

		File d = new File(dir);
		String files[] = d.list();
		if(files == null){
			Utility.writeToDebug("Error reading directory: " + dir);
		}
		int x = 0;
		while(files != null && x<files.length){
			if(files[x].matches(pattern)){
				count++;
			}
			x++;
		}
		
		return count;
	}

	public static String readFileIntoString(String filePath){
		Utility.writeToDebug("reading file: " + filePath); 
		String fileContents = "";
		try{
			File fromFile = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(fromFile));
			char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=br.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileContents += readData;
	            buf = new char[1024];
	        }
	        br.close();

		}
		catch(Exception ex){
			Utility.writeToDebug(ex);
		}

		Utility.writeToDebug("finshed reading file: " + filePath + " (" + fileContents.length() / 1000000 + " megabytes)"); 
		return fileContents;
	}

	public static void writeStringToFile(String text, String filePath){
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			out.write(text);
			out.close();
		}
		catch(Exception ex){
			Utility.writeToDebug(ex);
		}
	}
	
	public static void makeDirContentsExecutable(String fromDir){
		try{
			File dir = new File(fromDir);
			String files[] = dir.list();
			if(files == null){
				Utility.writeToDebug("Error reading directory: " + fromDir);
			}
			int x = 0;
			while(files != null && x<files.length){
				File xfile = new File(fromDir + files[x]);
				if(! xfile.isDirectory()){
					boolean success = xfile.setExecutable(true);
				}
				x++;
			}
		}
		catch(Exception ex){
			Utility.writeToDebug(ex);
		}
	}
	
	public static void copyDirContents(String fromDir, String toDir, boolean recurse){
		try{
			if(!fromDir.endsWith("/")){
				fromDir += "/";
			}
			if(!toDir.endsWith("/")){
				toDir += "/";
			}
			File dir = new File(fromDir);
			String files[] = dir.list();
			if(files == null){
				Utility.writeToDebug("Error reading directory: " + fromDir);
			}
			int x = 0;
			while(files != null && x<files.length){
				File xfile = new File(fromDir + files[x]);
				if(! xfile.isDirectory()){
					FileChannel ic = new FileInputStream(fromDir + files[x]).getChannel();
					FileChannel oc = new FileOutputStream(toDir + files[x]).getChannel();
					ic.transferTo(0, ic.size(), oc);
					ic.close();
					oc.close(); 
				}
				else{
					//we hit a subdirectory. Recurse down into it if needed, otherwise ignore it.
					if(recurse){
						File newDir = new File(toDir + files[x]);
						newDir.mkdir();
						copyDirContents(fromDir + files[x], toDir + files[x], true);
					}
				}
				x++;
			}
		}catch(Exception ex){
			Utility.writeToDebug(ex);
		}
	}

	/**
	 * Should be used when modeling or prediction job was started with uploaded files
	 * but after uploadDataset
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	public static void copyFile(String from, String to) throws IOException{
		  File fromFile = new File(from);
		    File toFile = new File(to);

		    if (!fromFile.exists())
		      throw new IOException("FileCopy: " + "no such source file: "
		          + from);
		    if (!fromFile.isFile())
		      throw new IOException("FileCopy: " + "can't copy directory: "
		          + from);
		    if (!fromFile.canRead())
		      throw new IOException("FileCopy: " + "source file is unreadable: "
		          + from);

		    if (toFile.isDirectory())
		      toFile = new File(toFile, fromFile.getName());

		    if (toFile.exists()) {
		      if (!toFile.canWrite())
		        throw new IOException("FileCopy: "
		            + "destination file is unwriteable: " + to);
		      
		      String parent = toFile.getParent();
		      File dir = new File(parent);
		      if (!dir.exists())
		        throw new IOException("FileCopy: "
		            + "destination directory doesn't exist: " + parent);
		      if (dir.isFile())
		        throw new IOException("FileCopy: "
		            + "destination is not a directory: " + parent);
		      if (!dir.canWrite())
		        throw new IOException("FileCopy: "
		            + "destination directory is unwriteable: " + parent);
		    }
		    else toFile.createNewFile();

		    FileInputStream from_ = null;
		    FileOutputStream to_ = null;
		    try {
		      from_ = new FileInputStream(fromFile);
		      to_ = new FileOutputStream(toFile);
		      byte[] buffer = new byte[4096];
		      int bytesRead;

		      while ((bytesRead = from_.read(buffer)) != -1)
		        to_.write(buffer, 0, bytesRead); // write
		    } finally {
		      if (from_ != null)
		        from_.close();
		      if (to_ != null)
		          to_.close();
		     }
		 }
	

	public static void moveFile(String fromPath, String toPath){
		//not here yet, cause exec("mv") works fine
		//actually exec("mv") works much faster!
	}
	
	public static void deleteFile(String filePath) {
		try{
		    // A File object to represent the filename
		    File f = new File(filePath);
	
		    // Make sure the file or directory exists and isn't write protected
		    if (!f.exists())
		      throw new IllegalArgumentException(
		          "Delete: no such file or directory: " + filePath);
	
		    if (!f.canWrite())
		      throw new IllegalArgumentException("Delete: write protected: "
		          + filePath);
	
		    // Attempt to delete it
		    boolean success = f.delete();
	
		    if (!success){
		      throw new IllegalArgumentException("Delete: deletion failed");
		    }
		}
		catch(Exception ex){
			Utility.writeToDebug(ex);
		}
	}
	
	public static void deleteDirContents(String dirToErase){
		//Removes all files in a directory.
		//For safety reasons, this function is not recursive. 
		//(Don't want anyone to delete the whole filesystem by accident.)
		
		if(!dirToErase.endsWith("/")){
			dirToErase += "/";
		}
		
		File dir = new File(dirToErase);
		try{
			String files[] = dir.list();
			if(files != null){
				Utility.writeToDebug("Deleting " + files.length + " files from dir: " + dirToErase);
			}
			else{
				Utility.writeToDebug("Could not open dir: " + dirToErase);
			}
			int x = 0;
			while(files != null && x<files.length){
				if(! (new File(dirToErase + files[x])).isDirectory()){
					deleteFile(dirToErase + files[x]);
				}
				x++;
			}
		}
		catch(Exception ex){
			Utility.writeToDebug(ex);
		}
	}

	public static void writeFiles(InputStream is, String fullFileLocation)
			throws IOException {
		OutputStream bos = new FileOutputStream(fullFileLocation);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
			bos.write(buffer, 0, bytesRead);
		}
		bos.close();
		is.close();
	}
	
	public static boolean deleteDir(File dir) {
		//recursive as hell! Deletes everything in dir! Be careful!
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                   return false;
                }
            }
        }
        
        // The directory is now empty so delete it
        return dir.delete();
    }
	
}