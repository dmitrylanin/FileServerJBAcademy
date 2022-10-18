package server;

import java.io.*;

import java.util.TreeMap;

/*
    Файловый индекс - храним в оперативной памяти статусы файлов + записываем статус в файл
 */

public class FileIndex{
    protected TreeMap<Integer, String> activeTreeFilesIndex;
    protected TreeMap <String, Integer> activeMirrorFilesIndex;
    protected TreeMap<Integer, String> deletedTreeFilesIndex;
    protected TreeMap <String, Integer> deletedMirrorFilesIndex;
    private File fileIndexInFile;
    private int lastId = 1;

    FileIndex(File basicDirectory){
        this.fileIndexInFile = new File(basicDirectory+ File.separator +"fileIndex.txt");
        //Проверка - если файл с индексом существует, НЕ перезаписывем
        if(!fileIndexInFile.exists()){
            try(FileOutputStream fileIndexInFileOutputStream = new FileOutputStream(basicDirectory+ File.separator +"fileIndex.txt")){
            }catch (Exception e){
                System.out.println("Класс FileIndex НЕ смог создать файл с индексом");
            }
        }

        //Создаем структуры для хранения индексов в памяти
        this.activeTreeFilesIndex = new TreeMap<>();
        this.activeMirrorFilesIndex = new TreeMap<>();
        this.deletedTreeFilesIndex = new TreeMap<>();
        this.deletedMirrorFilesIndex = new TreeMap<>();

        //Записываем данные из файла в структуры
        try(BufferedReader fileIndexReader = new BufferedReader(new FileReader(this.fileIndexInFile))){
            String line;
            int id = 0;
        //Использую __!__ в качестве разделителя
        //Числовой индекс__!__Названий файла__!__Статус
            while ((line = fileIndexReader.readLine()) != null){
                String [] lineArr = line.split("__!__");
                if(lineArr[2].equals("active")){
                    activeTreeFilesIndex.put((id = Integer.parseInt((lineArr[0]))), lineArr[1]);
                    activeMirrorFilesIndex.put(lineArr[1], Integer.parseInt((lineArr[0])));
                }else{
                    deletedTreeFilesIndex.put((id = Integer.parseInt((lineArr[0]))), lineArr[1]);
                    deletedMirrorFilesIndex.put(lineArr[1], Integer.parseInt((lineArr[0])));
                }
        //Сохраняем в памяти последний id файла
                this.lastId = id+1;
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    protected synchronized void deleteIdFromFileIndex(Integer id, String fileName){
        //Нахожу данные для формирования пары - ключ-значение и удаляю пару из активного индекса
        if(id == null){
            id = activeMirrorFilesIndex.get(fileName);
            activeMirrorFilesIndex.remove(fileName);
            activeTreeFilesIndex.remove(id);
        }else{
            fileName = activeTreeFilesIndex.get(id);
            activeMirrorFilesIndex.remove(fileName);
            activeTreeFilesIndex.remove(id);
        }

        //Добавляю пару в индекс удаленных элементов
        deletedTreeFilesIndex.put(id, fileName);
        deletedMirrorFilesIndex.put(fileName, id);

        //Перезаписываю индексный файл с новым статусом пары
        try(FileWriter writer = new FileWriter(fileIndexInFile, false)){
            for (int i = 1; i<lastId; i++){
                String str;

                if(activeTreeFilesIndex.containsKey(i)){
                    str = i + "__!__" + activeTreeFilesIndex.get(i) + "__!__" + "active" + "\n";
                }else{
                    str = i + "__!__" + deletedTreeFilesIndex.get(i) + "__!__" + "delete" + "\n";
                }
                writer.write(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected synchronized int addNewFileToIndex(String fileName){
        boolean marker = true;
        try (BufferedWriter writerToFileIndex = new BufferedWriter(new FileWriter(fileIndexInFile, true))){
            writerToFileIndex.write(this.lastId + "__!__" + fileName + "__!__" + "active"+"\n");
            activeTreeFilesIndex.put(lastId, fileName);
            activeMirrorFilesIndex.put(fileName, this.lastId);
        } catch (IOException e) {
            marker = false;
            e.printStackTrace();
        }finally {
            if(marker) {
                this.lastId += 1;
                return lastId-1;
            }else{
                return 0;
            }
        }
    }
}