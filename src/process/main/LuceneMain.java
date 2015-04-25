package process.main;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import util.AppUtil;
import util.DbUtil;

/**
 * 将路线存入数据库并解析为lucene格式
 * @author yinchuandong
 *
 */
public class LuceneMain {

	Analyzer analyzer = null;
	Directory directory = null;
	IndexWriter iWriter = null;
	IndexReader iReader = null;
	IndexSearcher iSeacher = null;

	public LuceneMain() {
		try {
			analyzer = new IKAnalyzer(false);
			directory = new SimpleFSDirectory(new File("./index/"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private IndexWriter getWriter() throws IOException {
		IndexWriterConfig iWConfig = new IndexWriterConfig(Version.LUCENE_48,
				analyzer);
		// 每次都重新创建索引，否则可选create_or_append
		iWConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter iWriter = new IndexWriter(directory, iWConfig);
		return iWriter;
	}

	public void saveToDB(File rootDir) {

		File[] cityDirs = rootDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {

				return pathname.isDirectory();
			}
		});

		for (File cityDir : cityDirs) {
			int i = 0;
			File[] files = cityDir.listFiles();
			for (File file : files) {
				String jsonStr = AppUtil.readFile(file);
//				System.out.println((++i) + "-" + jsonStr);
				JSONObject obj = JSONObject.fromObject(jsonStr);
				System.out.println(obj.get("sname"));
				String uid = obj.getString("uid");
				String sid = obj.getString("sid");
				String surl = obj.getString("surl");
				String sname = obj.getString("sname");
				String ambiguitySname = obj.getString("ambiguitySname");
				double hotness = obj.getDouble("hotness");
				int viewCount = obj.getInt("viewCount");
				double sumPrice = obj.getDouble("sumPrice");
				double hotelPrice = obj.getDouble("hotelPrice");
				double sceneTicket = obj.getDouble("sceneTicket");
				double minDay = obj.getDouble("minDay");
				double maxDay = obj.getDouble("maxDay");
				double visitDay = obj.getDouble("visitDay");
				double distance = obj.getDouble("distance");
				String jsonName = cityDir.getName()+"/"+file.getName();
				String arrange = obj.getJSONArray("arrange").toString();
				JSONArray sceneListArr = obj.getJSONArray("sceneryList");
				String keyword = "";
				for (int j = 0; j < sceneListArr.size(); j++) {
					keyword += sceneListArr.getJSONObject(j).getString("sname")+",";
				}
				keyword = keyword.substring(0, keyword.length() - 1);
				
				String sql = "insert into t_route values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String[] params = {
						uid, sid, surl, sname,ambiguitySname, hotness+"", viewCount+"",
						sumPrice+"", hotelPrice+"", sceneTicket+"", minDay+"", maxDay+"",
						visitDay+"", distance+"", jsonName, arrange, keyword
				};
				DbUtil.executeUpdate(sql, params);
			}
		}

		return;
	}


	public void saveToLucene(File rootDir) throws IOException{
		File[] cityDirs = rootDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {

				return pathname.isDirectory();
			}
		});
		iWriter = getWriter();
		for (File cityDir : cityDirs) {
			int i = 0;
			File[] files = cityDir.listFiles();
			for (File file : files) {
				Document doc = new Document();
				String jsonStr = AppUtil.readFile(file);
				JSONObject obj = JSONObject.fromObject(jsonStr);
				String uid = obj.getString("uid");
				String sid = obj.getString("sid");
				String surl = obj.getString("surl");
				String sname = obj.getString("sname");
				String ambiguitySname = obj.getString("ambiguitySname");
				double hotness = obj.getDouble("hotness");
				int viewCount = obj.getInt("viewCount");
				double sumPrice = obj.getDouble("sumPrice");
				double hotelPrice = obj.getDouble("hotelPrice");
				double sceneTicket = obj.getDouble("sceneTicket");
				double minDay = obj.getDouble("minDay");
				double maxDay = obj.getDouble("maxDay");
				double visitDay = obj.getDouble("visitDay");
				double distance = obj.getDouble("distance");
				String jsonName = cityDir.getName()+"/"+file.getName();
				String arrange = obj.getJSONArray("arrange").toString();
				JSONArray sceneListArr = obj.getJSONArray("sceneryList");
				String keyword = "";
				for (int j = 0; j < sceneListArr.size(); j++) {
					keyword += sceneListArr.getJSONObject(j).getString("sname")+",";
				}
				keyword = keyword.substring(0, keyword.length() - 1);
				
				Field fuid = new StoredField("uid", uid);
				Field fsid = new StoredField("sid", sid);
				Field fsurl = new StoredField("surl", surl);
				Field fsname = new StringField("sname", sname, Field.Store.YES);
				Field fambiguitySname = new StringField("ambiguitySname", ambiguitySname, Field.Store.YES);
				Field fhotness = new DoubleField("hotness", hotness, Field.Store.YES);
				Field fviewCount = new IntField("viewCount", viewCount, Field.Store.YES);
				Field fsumPrice = new DoubleField("sumPrice", sumPrice, Field.Store.YES);
				Field fhotelPrice = new DoubleField("hotelPrice", hotelPrice, Field.Store.YES);
				Field fsceneTicket = new DoubleField("sceneTicket", sceneTicket, Field.Store.YES);
				Field fminDay = new DoubleField("minDay", minDay, Field.Store.YES);
				Field fmaxDay = new DoubleField("maxDay", maxDay, Field.Store.YES);
				Field fvisitDay = new DoubleField("visitDay", visitDay, Field.Store.YES);
				Field fdistance = new DoubleField("distance", distance, Field.Store.YES);
				Field fjsonName = new StoredField("jsonName", jsonName);
				Field farrange = new StoredField("arrange", arrange);
				Field fkeyword = new TextField("keyword", keyword, Field.Store.NO);
				
				doc.add(fuid);
				doc.add(fsid);
				doc.add(fsurl);
				doc.add(fsname);
				doc.add(fambiguitySname);
				doc.add(fhotness);
				doc.add(fviewCount);
				doc.add(fsumPrice);
				doc.add(fhotelPrice);
				doc.add(fsceneTicket);
				doc.add(fminDay);
				doc.add(fmaxDay);
				doc.add(fvisitDay);
				doc.add(fdistance);
				doc.add(fjsonName);
				doc.add(farrange);
				doc.add(fkeyword);
				iWriter.addDocument(doc);
			}
		}
		iWriter.close();
		return;
	}

	public static void main(String[] args) throws IOException {
		 LuceneMain luceneMain = new LuceneMain();
//		 luceneMain.saveToDB(new File("./routes"));
		 luceneMain.saveToLucene(new File("./routes"));
	}
}
