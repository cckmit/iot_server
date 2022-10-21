//package com.aliyun.iotapigateway;
package com.aliyun.iotx.api.client;
//import com.taobao.pandora.boot.PandoraBootstrap;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

//import com.aliyun.iotapigateway.Client;
//import com.aliyun.iotapigateway.models.CommonParams;
//import com.aliyun.iotapigateway.models.Config;
//import com.aliyun.iotapigateway.models.IoTApiRequest;
//import com.aliyun.tea.TeaResponse;
//import com.aliyun.teautil.models.RuntimeOptions;
//import com.ly.util.MyX509TrustManager;
//import com.aliyun.iotapigateway.Client;
//import net.sf.json.JSONObject;
//import com.aliyun.iotx.api.client.SyncApiClient;
//import com.aliyun.iotx.api.client.IoTApiRequest;
//import com.aliyun.iotx.api.client.IoTApiRequest
import com.alibaba.fastjson.JSONObject;
import com.alibaba.cloudapi.sdk.model.ApiResponse;
import com.aliyun.iotx.api.client.SyncApiGetClient;
/**
 * Pandora Boot应用的入口类
 *
 * @author chengxu
 */

//@SpringBootApplication(scanBasePackages = {"com.aliyun.iotx"})
public class Application {


	//厂家id
	private static String tenantId ="BAB92BB8D18B4EB2AEAF416414E91926";
	//private static String apiUrl ="https://api.link.aliyun.com/";




	//创建产品接口
/*	public static String create()
	{
		String result="";
		try {

			Config config = new Config();
			config.appKey="33199735";
			config.appSecret="450a1cf9f9f7ded57ed6b1a7f6392e7f";
			config.readTimeout=10000;
			config.protocol = "https";
			config.connectTimeout = 10000;
			config.domain="api.link.aliyun.com";
			config.maxIdleConns=10000;

			Client ss  = new Client(config);

			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap.put("tenantId", "BAB92BB8D18B4EB2AEAF416414E91926");
			resMap.put("productName", "9999999999");
			resMap.put("productBrand", "闪骑侠1");
			resMap.put("productAlias", "ss111223344");
			resMap.put("productModel", "2GBLE60-SZDM11111145544");
			resMap.put("productAlgoType", "2");
			resMap.put("isSupport2G", false);
			resMap.put("isDisplayEnergy", 1);
			resMap.put("isDistantConfig", 1);
			resMap.put("sDisplayBicycleStatus", 1);
			resMap.put("isUsingStorageLock", 1);
			resMap.put("isSupportKeyModel", 1);
			resMap.put("defaultKeyModel", 1);
			CommonParams cs = new CommonParams();

			IoTApiRequest ii = new IoTApiRequest();
			ii.id= UUID.randomUUID().toString();
			ii.version="1.0";
			ii.params =  resMap;
			ii.request=cs;

			RuntimeOptions rr = new RuntimeOptions();

			Map<String, String> header = new HashMap<String, String>();
			header.put("X-CA-STAGE", "PRE");

			TeaResponse teaResponse = ss.doRequest("/haas/travel/open/product/create", "https", "POST", header, ii, rr);
			if(teaResponse!=null)
			{
				System.out.println("teaResponseteaResponse---"+teaResponse.statusCode+"---teaResponse.statusMessage--: "+teaResponse.statusMessage);
			}


		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
*/

	//查询接口
        public static String query()
        {
                String result="";
                try {

                      /*  Config config = new Config();
                        config.appKey="33199735";
                        config.appSecret="450a1cf9f9f7ded57ed6b1a7f6392e7f";
                        config.readTimeout=10000;
                        config.protocol = "https";
                        config.connectTimeout = 10000;
                        config.domain="api.link.aliyun.com";
                        config.maxIdleConns=10000;

                        Client ss  = new Client(config);

                        Map<String, Object> resMap = new HashMap<String, Object>();
                        resMap.put("tenantId", "0D6D5E0F70B2499FAF760166D36858EA");
                        resMap.put("productKey", "a1Dr44A5e2h");
			resMap.put("deviceName", "48:6e:70:4b:01:b6");
                        CommonParams cs = new CommonParams();

                        IoTApiRequest ii = new IoTApiRequest();
                        ii.id= UUID.randomUUID().toString();
                        ii.version="1.0.0";
                        ii.params =  resMap;
                        ii.request=cs;

                        RuntimeOptions rr = new RuntimeOptions();

                        Map<String, String> header = new HashMap<String, String>();
                        header.put("X-Ca-Stage", "PRE");
			//header.put("X-CA-STAGE", "PRE");
                        TeaResponse teaResponse = ss.doRequest("/haas/travel/open/device/time/bind", "https", "GET", header, ii, rr);
                        if(teaResponse!=null)
                        {
                                System.out.println("teaResponseteaResponse---"+teaResponse.statusCode+"---teaResponse.statusMessage--"+teaResponse.statusMessage);
                        	System.out.println("teaResponseteaResponse---"+teaResponse.statusCode+"--teaResponse.statusMessage--"+teaResponse.response.toString());
			System.out.println("teaResponseteaResponse---:teaResponse " + teaResponse.getResponseBody());
			}
			
			ApiResponse response = syncApiClient.postBody("api.link.aliyun.com", "/haas/travel/open/device/time/bind", ii, true, headers);

			System.out.println(
    "response code = " + teaResponse.response.getCode()
        + " response = " + new String(teaResponse.response.getBody(), "UTF-8")
        + " headers = " + teaResponseresponse.getHeaders().toString()
);*/
			  // https://github.com/aliyun/iotx-api-gateway-client
			IoTApiClientBuilderParams ioTApiClientBuilderParams = new IoTApiClientBuilderParams();
			
			ioTApiClientBuilderParams.setAppKey("33199735");
			ioTApiClientBuilderParams.setAppSecret("450a1cf9f9f7ded57ed6b1a7f6392e7f");

			//SyncApiClient syncApiClient = new SyncApiClient(ioTApiClientBuilderParams);
			SyncApiGetClient syncApiClient = new SyncApiGetClient(ioTApiClientBuilderParam);
			Map<String, String> request = new HashMap<>(8);

			// 设置请求ID
			String uuid = UUID.randomUUID().toString();
			String id = uuid.replace("-", "");
			//request.setId(id);
			// 设置API版本号
			//request.setApiVer("1.0.0");
			// 设置参数
			request.putParam("apiVer", "1.0.0");
        		request.putParam("id", UUID.randomUUID().toString());
			request.putParam("tenantId","0D6D5E0F70B2499FAF760166D36858EA");
			request.putParam("productKey","a1Dr44A5e2h");
			request.putParam("deviceName","48:6e:70:4b:01:b6");
			// 如果需要，设置headers
			Map<String, String> headers = new HashMap<String, String>(8);
			// headers.put("X-CA-STAGE", "PRE");

			// 设置请求参数域名、path、request , isHttps, headers
			//ApiResponse response = syncApiClient.postBody("api.link.aliyun.com", "/haas/travel/open/device/time/bind", request, true, headers);
			//doGet
			ApiResponse response = SyncApiGetClient.doGet("api.link.aliyun.com", "/haas/travel/open/device/time/bind", true, header,request);
			System.out.println("response code = " + response.getCode()
        			+ " response = " + new String(response.getBody(), "UTF-8")
        			+ " headers = " + response.getHeaders().toString()
				);

                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
                return result;
        }
/*
	public static void ApiRequest(){
	// https://github.com/aliyun/iotx-api-gateway-client
IoTApiClientBuilderParams ioTApiClientBuilderParams = new IoTApiClientBuilderParams();

ioTApiClientBuilderParams.setAppKey("你的<AppKey>");
ioTApiClientBuilderParams.setAppSecret("你的<AppSecret>");

SyncApiClient syncApiClient = new SyncApiClient(ioTApiClientBuilderParams);

IoTApiRequest request = new IoTApiRequest();

// 设置请求ID
String uuid = UUID.randomUUID().toString();
String id = uuid.replace("-", "");
request.setId(id);
// 设置API版本号
request.setApiVer("1.0.0");
// 设置参数
request.putParam("tenantId","value1");
request.putParam("productKey","value2");
request.putParam("deviceName","value3");
// 如果需要，设置headers
Map<String, String> headers = new HashMap<String, String>(8);
// headers.put("你的<header", "你的<value>");

// 设置请求参数域名、path、request , isHttps, headers
ApiResponse response = syncApiClient.postBody("api.link.aliyun.com", "/haas/travel/open/device/time/bind", request, true, headers);

System.out.println(
    "response code = " + response.getCode()
        + " response = " + new String(response.getBody(), "UTF-8")
        + " headers = " + response.getHeaders().toString()
);

	}*/
	//绑定时间接口
/*	public static String active()
	{
		String result="";
		try {


		Config config = new Config();
		config.appKey="33199735";
		config.appSecret="450a1cf9f9f7ded57ed6b1a7f6392e7f";
		config.readTimeout=10000;
		config.protocol = "https";
		config.connectTimeout = 10000;
		config.domain="api.link.aliyun.com";
		config.maxIdleConns=10000;

		Client ss  = new Client(config);

		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("productKey", "闪骑侠");
		resMap.put("deviceName", "48:6e:70:d9:a0:5a");

		CommonParams cs = new CommonParams();

		IoTApiRequest ii = new IoTApiRequest();
		ii.id= "11111";
		ii.version="sss";
		ii.params =  resMap;
		ii.request=cs;

		RuntimeOptions rr = new RuntimeOptions();

		Map<String, String> header = new HashMap<String, String>();

		TeaResponse teaResponse = ss.doRequest("/haas/travel/open/device/time/active", "https", "GET", header, ii, rr);
		if(teaResponse!=null)
		{
			System.out.println("teaResponseteaResponse---"+teaResponse.statusCode+"--teaResponse.statusMessage--"+teaResponse.statusMessage);

		System.out.println("teaResponseteaResponse---"+teaResponse.statusCode+"--teaResponse.statusMessage--"+teaResponse.response.toString());
		System.out.println("teaResponseteaResponse---:teaResponse " + teaResponse.body.toString());
		}





		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

*/

	public static void main(String[] args) {
		//		create();
		//active();
		query();
	}

}
