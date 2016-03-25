package heisenberg.com;


import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Camel Application
 */
public class XiaMiSpider {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    private final static String XIAMI_YINYUE_URL = "http://www.xiami.com/search?key=";
    public static void main(String... args) throws Exception {
        InputStream inp = null;
        OutputStream out = null;
        BufferedOutputStream bos = null;
        String songName="";
        String artist="";
        try{
            inp = new FileInputStream("E:\\LAIHeisenberg\\spider\\src\\main\\resources\\input2.xlsx");

            Workbook wb = WorkbookFactory.create(new BufferedInputStream(inp));
            Sheet sheet = wb.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for(int i = 0;i < lastRowNum+1;i++){
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(1);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                songName = cell.getStringCellValue();
                cell = row.getCell(2);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                artist = cell.getStringCellValue();
                if(artist == null || artist.length() <= 0 || songName == null || songName.length() <= 0){
                    continue;
                }
                int index;
                if((index = songName.indexOf("官方版")) != -1 || (index = songName.indexOf("<")) != -1 ||(index = songName.indexOf("《")) != -1){
                    songName = songName.substring(0,index);
                }
                if(artist.contains("音悦")){
                    continue;
                }

                String searchParam = songName+" "+artist;
                searchParam = URLEncoder.encode(searchParam,"UTF-8");
                String url = XIAMI_YINYUE_URL.concat(searchParam);
                Document document = getDocument(url);
                System.out.println("songName: "+songName+"  URL: "+url);
                url = getAlbumUrl(songName,artist,document);

                if(url != null){
                    document = getDocument(url);
                    String company = getAlbumInfo(document,songName);
                    if(company == null){
                        continue;
                    }
                    cell = row.getCell(4);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(company);

                    if(i % 50 == 0){
                        out = new FileOutputStream("E:\\LAIHeisenberg\\spider\\src\\main\\resources\\output.xlsx");
                        bos = new BufferedOutputStream(out);
                        wb.write(bos);
                    }

                }

                Thread.sleep(150);
            }
            out = new FileOutputStream("E:\\LAIHeisenberg\\spider\\src\\main\\resources\\output.xlsx");
            bos = new BufferedOutputStream(out);
            wb.write(bos);


        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(inp != null ){
                    inp.close();
                    out.close();
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    public static Document getDocument(String url){

        Document document = null;
        try{
            document = Jsoup.connect(url).timeout(45000).get();
            return document;
        }catch (IOException e){
            e.printStackTrace();
        }

        return document;
    }

    public static String getAlbumUrl(String songName, String artist, Document document){

        Element body = document.body();
        Element tbodys = body.select(".track_list").get(0).select("tbody").get(0);
        Elements songList = tbodys.select(".song_name");
        Elements artists = tbodys.select(".song_artist");
        Elements albums = tbodys.select(".song_album");

        String name = "";
        String arts = "";
        String album = null;
        HashMap<Integer,String> songMap = new HashMap<Integer, String>();
        for(int i = 0; i < songList.size();i++){
            try{
                arts = artists.get(i).select("a").get(0).text();
                Element b;
                if(( b = artists.get(i).select("a").select("b").get(0)) != null){
                    arts = arts.concat(b.text());
                }
                name = songList.get(i).select("b").get(0).text();
            }catch (Exception e){
                if(stringMatch(arts,artist)){
                    name = songList.get(i).select("a").get(0).text();
                }
            }finally {
                int songMatch = isMatch(songName,name);
                int artistMatch = isMatch(artist,arts);

                if (songMatch >= 1 && artistMatch >= 1){
                    album = albums.get(i).select("a").get(0).attr("href");
                    songMap.put(songMatch+artistMatch,album);
                }


               /* if(stringMatch(name,songName)){
                    if (stringMatch(artist,arts)){
                        album = albums.get(i).select("a").get(0).attr("href");
                        if(album == null || album.length() <= 1){

                        }
                        return album;
                    }
                }
                else if (stringMatch(artist,arts)){
                    album = albums.get(i).select("a").get(0).attr("href");
                    return album;
                }*/

            }
        }

        if (songMap.size() > 0){
            Set<Map.Entry<Integer, String>> entries = songMap.entrySet();
            int max = 0;
            for (Map.Entry<Integer,String> entry : entries){
                Integer key = entry.getKey();
                if (max < key){
                    max = key;
                }
            }
            album = songMap.get(max);
            System.out.println("name: "+name+" arts: "+arts);
        }

        return album;
    }

    public static String getAlbumInfo(Document document,String songName){
        Element body = document.body();
        String albumName = body.select("h1").get(0).text();
        String company = "";
        try{
            Element tbody = body.select("#content").select("#main_wrapper").select("#main").select("#album_info").select("table").select("tbody").get(0);

            String artist = tbody.select("a").get(0).text();
            company = tbody.select("a").get(1).text();
            if(company == null || company.length() < 1){
                company = tbody.select("a").get(2).text();
            }

            System.out.println("歌曲: "+songName+"  专辑："+albumName+"  艺人: "+artist+"   唱片公司: "+company);
//            Elements tds = tbody.select("td");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return company;
    }


    public static int isMatch(String str1,String str2){

        if(str1 == null || str1.length() < 1 || str2 == null || str2.length() < 1){
            return 0;
        }

        //字符串匹配度
        int matchingRate = 0;

        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        if (str1.contains(str2) || str2.contains(str1)){
            return 20;
        }
        if(str1.length() < str2.length()){
            String temp = str2;
            str2 = str1;
            str1 = temp;
        }
        int hit = 0;
        for (int i = 0; i < str2.length(); i++){
            boolean flag = str1.contains(str2.charAt(i)+"");
            if (flag){
                hit++;
                if(hit >= 2){
                    matchingRate++;
                }
            }else{
                hit = 0;
            }
        }
        return  matchingRate;
    }


    public static boolean stringMatch(String str1,String str2){

        if(str1 == null || str1.length() < 1 || str2 == null || str2.length() < 1){
            return false;
        }
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        if(str1.contains(str2)){
            return true;
        }else if(str2.contains(str1)){
            return true;
        }else {
            if(str1.length() < str2.length()){
                String temp = str2;
                str2 = str1;
                str1 = temp;
            }
            int hit = 0;
            for(int i = 0; i < str2.length(); i++){
                String s = str2.charAt(i)+"";
                boolean isMatch = str1.contains(s);
                if(isMatch){
                    hit++;
                }
            }
            if(hit >= 2){
                return true;
            }
            return false;
        }
    }

}

