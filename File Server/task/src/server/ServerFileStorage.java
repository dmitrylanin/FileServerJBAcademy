package server;

import java.io.*;

/*
    Класс, описывающий взаимодействие полученных от сокета потоков с файловой системой
 */

public class ServerFileStorage {
    private File directory;
    private FileIndex fileIndex;

    ServerFileStorage(){
        this.directory = new File(System.getProperty("user.dir")
                + File.separator + "src" + File.separator + "server" + File.separator + "data" + File.separator);
        if (!this.directory.exists()){
            this.directory.mkdirs();
        }
        this.fileIndex = new FileIndex(this.directory);
    }

    protected synchronized void saveNewFile(String fileName,
                                            DataInputStream inputStream,
                                            DataOutputStream output) throws IOException {
        int messageLength;
        byte [] buffer = new byte[128];
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(directory+System.getProperty("file.separator")+fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

            while (true){
                messageLength = inputStream.readInt();
                if (messageLength == -1){
                    break;
                }
                inputStream.read(buffer);
                fileOutputStream.write(buffer, 0, messageLength);
            }
            int newFileIndex = fileIndex.addNewFileToIndex(fileName);
            output.writeInt(200);
            output.writeUTF("Response says that file is saved! ID = " + newFileIndex + " \n");

    }


    protected synchronized void sendFileToClient(String fileName,
                                                 Integer fileId,
                                                 DataOutputStream outputSocketStream) throws IOException {
        if(fileName == null){
            fileName = fileIndex.activeTreeFilesIndex.get(fileId);
        }

        try(FileInputStream fileInputStream = new FileInputStream(directory+File.separator+fileName)){
            outputSocketStream.writeInt(200);
            int size = 0;
            byte[] buffer = new byte[128];
            while ((size = fileInputStream.read(buffer)) != -1) {
                outputSocketStream.writeInt(size);
                outputSocketStream.write(buffer, 0, buffer.length);
            }
            outputSocketStream.writeInt(size);
        }
    }


    protected synchronized boolean isFileIn(String fileName, Integer fileId){
        if(fileName == null){
            if(fileIndex.activeTreeFilesIndex.containsKey(fileId)){
                return true;
            }
        }else if (fileIndex.activeMirrorFilesIndex.containsKey(fileName)){
            return true;
        }else {
            return false;
        }
        return false;
    }

    protected synchronized void deleteFile(String fileName, Integer fileId, DataOutputStream output) throws IOException {

            if (fileName == null) {
                if (!fileIndex.activeTreeFilesIndex.containsKey(fileId) && !fileIndex.deletedTreeFilesIndex.containsKey(fileId)) {
                    output.writeInt(404);
                }

                if (fileIndex.activeTreeFilesIndex.containsKey(fileId)) {
                    fileName = fileIndex.activeTreeFilesIndex.get(fileId);
                } else if (fileIndex.deletedTreeFilesIndex.containsKey(fileId)) {
                    fileName = fileIndex.deletedTreeFilesIndex.get(fileId);
                } else {
                    System.out.println("Неизвестная ошибка в методе deleteFile класса ServerFileStorage");
                    output.writeInt(404);
                }
            }

            if (fileIndex.deletedMirrorFilesIndex.containsKey(fileName)) {
            } else {
                File file = new File(this.directory + File.separator + fileName);
                if (file.delete()) {
                    fileIndex.deleteIdFromFileIndex(fileId, fileName);
                    output.writeInt(200);
                }
            }

    }
}