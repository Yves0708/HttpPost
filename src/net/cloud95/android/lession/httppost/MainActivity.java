
package net.cloud95.android.lession.httppost;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity
{
	final static String ENTRY_POINT="http://yvesidv.netau.net//studentlist.php";
    //UI執行序用的 (跟介面有關的)
	private Handler mUI_Handler = new Handler();//此handler是直接和UI主執行續相連結
	private ListView listview;
	private HandlerThread mThread;
    //繁重執行序用的 (時間超過3秒的)
	private Handler mThreadHandler;
	private Handler mInsertThreadHandler;
	EditText studentid;
	EditText studentname;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listview = (ListView)findViewById(R.id.listView1);
		//這種執行序可以有名字!
		mThread = new HandlerThread("net");//和主執行續同一個階層
		mThread.start();
		refreshData();
    }
    private void refreshData(){
    	mThreadHandler = new Handler(mThread.getLooper());//此handler是和mThread這個執行續連結
		mThreadHandler.post(new Runnable()
		{
		    @Override
		    public void run()
		    {
				// TODO 自動產生的方法 Stub
		    	final String jsonString = executeQuery("getallstudents");
				mUI_Handler.post(new Runnable()
				{
				    @Override
				    public void run()
				    {
						// TODO 自動產生的方法 Stub
				    	renewListView(jsonString);
				    }
				});
		    }
		});
    }
    private String executeQuery(String opCode)
    {
		String result = "";
		try
		{
			HttpClient httpClient = new DefaultHttpClient();
		    //以 POST 要求
			HttpPost post = new HttpPost(ENTRY_POINT);
		    //準備參數列表
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    //加入參數
			nameValuePairs.add(new BasicNameValuePair("tag", opCode));
			nameValuePairs.add(new BasicNameValuePair("format", "json"));
		    //參數必須以特定編碼傳遞，如HTTP.UTF_8
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
		    //發送請求
			HttpResponse httpResponse = httpClient.execute(post);
		    //從  HttpResponse 取得進入物件
			HttpEntity httpEntity = httpResponse.getEntity();
		    //從 進入物件 取得傳輸通道
			InputStream inputStream = httpEntity.getContent();
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"),8);
			StringBuilder builder = new StringBuilder();
			String line = null;
		    //讀取每一行資料
			while((line = bufReader.readLine())!=null){
				builder.append(line+"\n");
			}
			inputStream.close();
			result = builder.toString();
			Log.e("String from server", result);
//			利用取代函式，消除 000webhost 頭尾的垃圾	
			result=result.replace("<table border='1' cellpadding='2' bgcolor='#FFFFDF' bordercolor='#E8B900' align='center'><tr><td><font face='Arial' size='2' color='#000000'><b>This website was set to be removed for inactivity by <a href='http://www.000webhost.com/'>www.000webhost.com</a>. If you own this website, <a href='http://www.000webhost.com/protect-website'>click here</a> to protect it.</b></font></td></tr></table><br />", "");
		    result=result.replace("<!-- Hosting24 Analytics Code -->", "");
		    result=result.replace("<script type=\"text/javascript\" src=\"http://stats.hosting24.com/count.php\"></script>", "");
		    result=result.replace("<!-- End Of Analytics Code -->", "");
		    result=result.replace("[\"VALUE\",\"allstudents\",[[\"\",\"\"],[\"\",\"\"],[\"\",\"\"],", "");
		    result=result.replace("]]]", "]");
		}
		catch (Exception e)
		{
		    Log.e("log_tag", e.toString());
		}
		return result;
    }
    //新增資料函式
    private void executeQuery(String opCode, String arg1, String arg2)
    {
		try
		{
			HttpClient httpClient = new DefaultHttpClient();
		    //以 POST 要求
			HttpPost post = new HttpPost(ENTRY_POINT);
		    //準備參數列表
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    //加入參數
			nameValuePairs.add(new BasicNameValuePair("tag", opCode));
			nameValuePairs.add(new BasicNameValuePair("format", "json"));
			nameValuePairs.add(new BasicNameValuePair("studentid", arg1));
			nameValuePairs.add(new BasicNameValuePair("studentname", arg2));
		    //參數必須以特定編碼傳遞，如HTTP.UTF_8
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
		    //發送請求
			HttpResponse httpResponse = httpClient.execute(post);
		    //根據回傳狀態碼判斷是否成功
			if(httpResponse.getStatusLine().getStatusCode()==200){
				Toast.makeText(this, arg1+":"+arg2+":insert Success", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, arg1+":"+arg2+":insert Failure", Toast.LENGTH_SHORT).show();
			}
		}
		catch (Exception e)
		{
		    Log.e("log_tag", e.toString());
		}
    }

    public final void renewListView(String input)
    {
	/*
	 * SQL 結果有多筆資料時使用JSONArray
	 * 只有一筆資料時直接建立JSONObject物件
	 * JSONObject jsonData = new JSONObject(result);
	 */
    	Log.e("Origin String", input);
		try
		{
			//最外層物件
			JSONArray jsonArray = new JSONArray(input);
		    //ListView 的資料源
			ArrayList<HashMap<String,Object>> users = new ArrayList<HashMap<String,Object>>();
		    //在標題顯示查詢到的筆數
			setTitle(jsonArray.length()+"筆資料");
			for(int i=0; i<jsonArray.length();i++){
				//取出第 i 個物件
				JSONObject jsonData = jsonArray.getJSONObject(i);
				//準備加入 ListView 資料源的元素
				HashMap<String,Object> h2 = new HashMap<String, Object>();
				//對將加入 ListView 資料源的元素，新增屬性
				h2.put("studentid", jsonData.getString("studentid"));
				h2.put("studentname", jsonData.getString("studentname"));
				//將元素加入 ListView 資料源
				users.add(h2);
			}
				//準備 Adapter
			SimpleAdapter adapter = new SimpleAdapter(this, users, R.layout.list, new String[]{"studentname","studentid"}, new int[]{R.id.textView1,R.id.textView2});
				//連接 Adapter 與 ListView
			listview.setAdapter(adapter);
		}
		catch (JSONException e)
		{
		    // TODO 自動產生的 catch 區塊
		    e.printStackTrace();
		}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 取得載入選單用的MenuInflater物件
        MenuInflater menuInflater = getMenuInflater();
        // 呼叫inflate方法載入指定的選單資源，第二個參數是這個方法的Menu物件
        menuInflater.inflate(R.menu.main, menu);
        // 回傳true選單才會顯示
        return true;
    }    
    
    // 參數MenuItem是使用者選擇的選單項目物件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 取得選單項目的資源編號
        int id = item.getItemId();
        
        switch (id) {
        // 新增
        case R.id.action_insert:{
            // 取得載入畫面配置資源用的物件
            // 載入對話框使用的畫面配置資源
            // 建立對話框物件
            // 設定對話框使用的畫面配置資源與標題
            // 加入登入按鈕
                        // 讀取帳號與密碼元件
                		mInsertThreadHandler = new Handler(mThread.getLooper());
                		mInsertThreadHandler.post(new Runnable()
                		{
                		    @Override
                		    public void run()
                		    {
                				// TODO 自動產生的方法 Stub
                				mUI_Handler.post(new Runnable()
                				{
                				    @Override
                				    public void run()
                				    {
                						// TODO 自動產生的方法 Stub
                				    }
                				});
                		    }
                		});
                    }
                });
            
            // 加入取消按鈕
            d.setNegativeButton(android.R.string.cancel, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
            
            // 顯示對話框
            d.show();
        	}
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
