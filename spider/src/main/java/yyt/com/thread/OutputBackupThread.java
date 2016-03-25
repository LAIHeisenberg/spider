package yyt.com.thread;

import java.io.*;

/**
 * Created by YinYueTai-DEV on 2016/3/24.
 */
public class OutputBackupThread extends Thread{

    private static final String INPUT_PATH = "E:\\LAIHeisenberg\\spider\\src\\main\\resources\\out.txt";
    private static final String OUTPUT_PATH = "E:\\LAIHeisenberg\\spider\\src\\main\\resources\\out_bak.txt";
    private static int begin = 99201;
    private static int offset;


    public void setBegin(int begin){
        this.begin = begin;
    }

    public void setOffset(int offset){
        this.offset = offset;
    }



    public OutputBackupThread(int offset){
        this.offset = offset;
    }

    @Override
    public void run(){

        File inFile = null;
        File outFile = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        try{
            inFile = new File(INPUT_PATH);
            outFile = new File(OUTPUT_PATH);
            br = new BufferedReader(new FileReader(inFile));
            bw = new BufferedWriter(new FileWriter(outFile,true));

            String line = "";
            for (int i = 1;i <= begin;i++){
                line = br.readLine();
            }


            for (int i = begin; i<= offset;i++){
                bw.write(line);
                bw.newLine();
                if (i % 200 == 0){
                    bw.flush();
                }
                line = br.readLine();
            }
            begin = offset+1;
            bw.flush();

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (br != null || bw != null){
                try{
                    br.close();
                    bw.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }

    }
}
