package utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/5/25.
 * 文件查询
 */
public class FindFileVisitor  extends SimpleFileVisitor<Path> {
    private ArrayList<String> fileList = new ArrayList<>();
    private Path homeDir;
    private String fileName = null;
    public FindFileVisitor(String homeDir) {
       this.homeDir = Paths.get(homeDir);
    }
    public FindFileVisitor setFindFileName(String fileName){
        this.fileName = fileName;
        return this;
    }
    public ArrayList<String> find(){
        try {
            long time = System.currentTimeMillis();
            Files.walkFileTree(homeDir, this);
            System.out.println("耗时:" + (System.currentTimeMillis() - time) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (file.toFile().getName().equals(fileName)) {
            fileList.add(file.toString());
        }
        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return FileVisitResult.SKIP_SUBTREE;
    }


}
