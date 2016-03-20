package heisenberg.com;

import org.apache.camel.main.Main;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.*;
import java.net.URLEncoder;

/**
 * A Camel Application
 */
public class MainApp {

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
            inp = new FileInputStream("F:\\spider\\src\\main\\resources\\input.xlsx");
            out = new FileOutputStream("F:\\spider\\src\\main\\resources\\out.xlsx");
            bos = new BufferedOutputStream(out);
            Workbook wb = WorkbookFactory.create(new BufferedInputStream(inp));
            Sheet sheet = wb.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for(int i = 0;i < lastRowNum+1;i++){
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(1);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                songName = cell.getStringCellValue().toLowerCase();
                cell = row.getCell(2);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                artist = cell.getStringCellValue().toLowerCase();

                int index;
                if((index = songName.indexOf("官方版")) != -1 || (index = songName.indexOf("<")) != -1 ||(index = songName.indexOf("《")) != -1){
                    songName = songName.substring(0,index);
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
                    cell = row.getCell(4);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(company);

                }

                Thread.sleep(500);
            }
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
            document = Jsoup.connect(url).timeout(30000).get();
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
        for(int i = 0; i < songList.size();i++){
            try{
                arts = artists.get(i).select("a").get(0).text();
                Element b;
                if(( b = artists.get(i).select("a").select("b").get(0)) != null){
                    arts = arts.concat(b.text()).toLowerCase();
                }
                name = songList.get(i).select("b").get(0).text().toLowerCase();
            }catch (Exception e){
                if(artist.contains(arts) || arts.contains(artist)){
                    name = songList.get(i).select("a").get(0).text();
                }
            }finally {
                if(songName != null && arts != null){
                    if(songName.contains(name) || name.contains(songName)){
                        if (artist.contains(arts) || arts.contains(artist)){
                            album = albums.get(i).select("a").get(0).attr("href");
                            return album;
                        }
                    }
                }

            }
        }
        return album;
    }

    public static String getAlbumInfo(Document document,String songName){
        Element body = document.body();
        String albumName = body.select("h1").get(0).text();
        Element tbody = body.select("#content").select("#main_wrapper").select("#main").select("#album_info").select("table").select("tbody").get(0);

        String artist = tbody.select("a").get(0).text();
        String company = tbody.select("a").get(1).text();

        System.out.println("歌曲: "+songName+"  专辑："+albumName+"  艺人: "+artist+"   唱片公司: "+company);
        Elements tds = tbody.select("td");
        for(Element td : tds ){
//            System.out.println(td.text());
        }
        return company;
    }

}

