package yyt.com;


import org.apache.poi.sl.draw.binding.ObjectFactory;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yyt.com.pojo.AlbumInfo;
import yyt.com.thread.OutputBackupThread;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * A Camel Application
 */
public class XiaMiSpider2 {
    private final static String XIAMI_YINYUE_URL = "http://www.xiami.com";
    private final static String XIAMI_YINYUE_SEARCH_URL = "http://www.xiami.com/search?key=";
    private final static String INPUT_PATH = "E:\\LAIHeisenberg\\spider\\src\\main\\resources\\mv320000.txt";
    private final static String OUTPUT_PATH = "E:\\LAIHeisenberg\\spider\\src\\main\\resources\\out.txt";
    private final static int START_LINE = 99201;
    private final static int END_LINE = 320000;

    public static void main(String... args) throws Exception {
        File file = null;
        BufferedReader br = null;
        String searchParam = "";
        String songName = "";
        String artist = "";
        Map<String,AlbumInfo> map = new LinkedHashMap<String, AlbumInfo>();
        try {
            file = new File(INPUT_PATH);
            br = new BufferedReader(new FileReader(file));
            String line = "";
            for (int i = 1;i <= START_LINE;i++){
                line = br.readLine();
            }

            int index = START_LINE;
            while (index <= END_LINE) {

                String[] searchParamArr = parseKeyword(line);
                AlbumInfo albumInfo = null;
                songName = searchParamArr[1];
                artist = searchParamArr[2];
                searchParam = searchParamArr[0];

                Document document = getDocument(searchParam);
                String albumUrl = getAlbumUrl(songName, artist, document);

                if (albumUrl != null) {
                    document = getDocument(albumUrl);
                    if (document != null){
                        albumInfo = getAlbumInfo(document);
                        if (albumInfo != null){
                            albumInfo.setAlbumUrl(albumUrl);
                            System.out.println(index+"   专辑: "+albumInfo.getAlbumName()+"  唱片公司: "+albumInfo.getCompany()+"  URL: "+albumInfo.getAlbumUrl());
                        }

                    }
                }
                map.put(searchParamArr[3],albumInfo);
                if (index % 100 == 0){
                    write_to_file(new File(OUTPUT_PATH),map);
                    map.clear();

                }
                if (index % 1000 == 0){
                    OutputBackupThread outputBackupThread = new OutputBackupThread(index);
                    outputBackupThread.start();
                    outputBackupThread.setBegin(index);
                }
                index++;
                line = br.readLine();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {

            write_to_file(new File(OUTPUT_PATH),map);

            if (br != null){
                br.close();
            }
        }

    }



    public static Document getDocument(String url){
        try{
            if (!url.startsWith(XIAMI_YINYUE_URL)){
                String param = URLEncoder.encode(url,"UTF-8");
                url = XIAMI_YINYUE_SEARCH_URL.concat(param);
            }
            Document document = Jsoup.connect(url).timeout(45000).get();
            return document;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }

    public static String getAlbumUrl(String songName, String artist, Document document){

        HashMap<Integer,String> songMap = new HashMap<Integer, String>();
        String album = null;
        try{
            Element body = document.body();
            Element track_list = body.select(".track_list").get(0);
            Elements tbodys = track_list.select("tbody");

            //虾米音乐 识别判断可能是正确的歌曲名
            Element search_box = body.select(".search_box").get(0);
            if (search_box != null){
                Element aTag = search_box.select("a").first();
                String xiamiTip  = aTag.text().toLowerCase().trim();

                if (artist.contains(xiamiTip)){
                    //如果虾米搜索推荐的是艺人名字 则不接受虾米推荐
                    //还是默认的搜索关键字 songName artist
                }else {
                    int tipMatch = isMatch(xiamiTip,songName+artist);
                    if (tipMatch >= 2){       //歌曲名+艺人名 最起码有四个字的匹配程度
                        songName = xiamiTip;
                        artist = xiamiTip;
                        document = getDocument(songName);
                        body = document.body();
                        track_list = body.select(".track_list").get(0);
                        tbodys = track_list.select("tbody");
                        System.out.println("虾米搜索自动匹配推荐  Url:"+XIAMI_YINYUE_SEARCH_URL.concat(URLEncoder.encode(songName,"UTF-8")));
                    }
                }
            }
            searchTbodyMatchSongName(tbodys,songName,artist,songMap);

        }catch (Exception e){
            e.printStackTrace();
            return null;
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
        }
        return album;
    }

    /**
     * 虾米音乐搜索页面 tbody内搜索匹配歌曲名
     *
     * @return
     */

    public static HashMap<Integer,String> searchTbodyMatchSongName(Elements tbodys,String songName,String artist,HashMap<Integer,String> songMap){

        Elements songNames = tbodys.select(".song_name");
        List<Element> songList = new ArrayList<Element>();

        for (Element song_name : songNames){
            //获取搜索后虾米音乐搜索自动给标红的歌曲名关键字
            Elements songNameKeyreds = song_name.select(".key_red");


            for (int i = 0;i<songNameKeyreds.size();i++){
                Element songParent = songNameKeyreds.get(i).parent();
                if (!songList.contains(songParent) ){
                    songList.add(songParent);
                }
            }
        }
        for (Element aTagElem : songList){
            Element songNameElem = aTagElem.parent();
            Element artistElem = songNameElem.nextElementSibling();
            Element albumElem = artistElem.nextElementSibling();
            if (artistElem != null && albumElem != null){
                String name = aTagElem.text();
                String arts = artistElem.select("a").get(0).text();
                String album = albumElem.select("a").get(0).attr("href");

                int songMatch = isMatch(songName,name);
                int artistMatch = isMatch(artist,arts);

                if (songMatch >= 1 && artistMatch >= 1){
                    songMap.put(songMatch+artistMatch,album);
                }
            }

        }
        if (songMap.size() > 0){
            return songMap;
        }else {
            //如果虾米搜索没有自动匹配到关键字，则使用旧的方法遍历整个搜索结果列表
            for (Element tbody : tbodys){
                searchTbodyMatchSongNameOld(tbody,songName,artist,songMap);
            }
        }
        return songMap;
    }

    //旧的遍历整个
    public static HashMap<Integer,String> searchTbodyMatchSongNameOld(Element tbody,String songName,String artist,HashMap<Integer,String> songMap){

        Elements songList = tbody.select(".song_name");
        Elements artists = tbody.select(".song_artist");
        Elements albums = tbody.select(".song_album");

        String name = "";
        String arts = "";
        String album = null;

        for(int i = 0; i < songList.size();i++){
            try{
                arts = artists.get(i).select("a").get(0).text();
                Element b;
                if(( b = artists.get(i).select("a").select("b").get(0)) != null){
                    arts = arts.concat(b.text());
                }
                name = songList.get(i).select("b").get(0).text();
            }catch (Exception e){
                if(isMatch(arts,artist) >= 1){
                    name = songList.get(i).select("a").get(0).text();
                }
            }finally {
                int songMatch = isMatch(songName,name);
                int artistMatch = isMatch(artist,arts);

                if (songMatch >= 1 && artistMatch >= 1){
                    album = albums.get(i).select("a").get(0).attr("href");
                    songMap.put(songMatch+artistMatch,album);
                }

            }
        }
        return songMap;
    }

    public static AlbumInfo getAlbumInfo(Document document){
        Element body = document.body();
        String albumName = body.select("h1").get(0).text();
        String company = "";
        String[] infos = null;
        try{
            Element tbody = body.select("#content").select("#main_wrapper").select("#main").select("#album_info").select("table").select("tbody").get(0);

            Elements trs = tbody.select("tr");
            if (trs.size() >= 1){
                infos = new String[7];
                infos[0] = albumName;
                int i = 1;
                for (Element tr : trs){
                    Element baseNode = tr.select("td").get(1);
                    String text = baseNode.text();
                    if (text == null || text.length() < 1){
                        text = baseNode.select("a").text();
                    }
                    infos[i++] = text;
                }
            }

            AlbumInfo albumInfo = new AlbumInfo(infos);
            return albumInfo;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static int isMatch(String str1,String str2){

        if(str1 == null || str1.length() < 1 || str2 == null || str2.length() < 1){
            return 0;
        }
        str1 = str1.toLowerCase().trim();
        str2 = str2.toLowerCase().trim();
        if (str1.equals(str2)){
            return 100;
        }

        //字符串匹配度
        int matchingRate = 0;

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




    public static String[] parseKeyword(String line) throws UnsupportedEncodingException{

        String[] splitLine = line.split("\\{");
        String id = splitLine[0];
        String song = splitLine[1];
        String artist = "";

        StringBuffer sb = new StringBuffer(song+" ");

        if (splitLine.length > 2){
            String artistArr = splitLine[2];
            String[] artists = artistArr.split(";;");
            if (artists.length > 1){
                for (String art : artists){
                    artist = art.split(",,")[1];
                    sb.append(artist+" ");
                }
                artist = sb.toString().trim();
            }else {
                artist = artists[0].split(",,")[1];
                sb.append(artist);
            }
        }
        line = id+"}"+song+"}"+artist;
        String searchParam = sb.toString();

        String[] res = new String[4];

        res[0] = searchParam;
        res[1] = song;
        res[2] = artist;
        res[3] = line;
        System.out.println("songName: " + song +"   artist: "+artist+"search url: "+XIAMI_YINYUE_SEARCH_URL.concat(URLEncoder.encode(searchParam,"UTF-8")));
        return res;
    }

    public static void write_to_file(File file,Map<String,AlbumInfo> map){
        FileWriter fw = null;
        BufferedWriter bw = null;
        try{
            //以追加的形式写人文本文件
            fw = new FileWriter(file,true);
            bw = new BufferedWriter(fw);
            Set<Map.Entry<String, AlbumInfo>> entries = map.entrySet();
            for (Map.Entry entry :  entries){
                String keyStr  = (String) entry.getKey();
                Object value = entry.getValue();
                if (value != null){
                    String content = keyStr+"}"+value.toString();
                    bw.write(content);
                    bw.write("\n");
                }else {
                    bw.write(keyStr);
                    bw.write("\n");
                }

            }
            bw.flush();

        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if( fw != null || bw != null){
                try{
                    fw.close();
                    bw.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
    }

}

