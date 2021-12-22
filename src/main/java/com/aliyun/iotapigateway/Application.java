package com.aliyun.iotapigateway;

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

import com.aliyun.iotapigateway.Client;
import com.aliyun.iotapigateway.models.CommonParams;
import com.aliyun.iotapigateway.models.Config;
import com.aliyun.iotapigateway.models.IoTApiRequest;
import com.aliyun.tea.TeaResponse;
import com.aliyun.teautil.models.RuntimeOptions;
//import com.ly.util.MyX509TrustManager;

//import net.sf.json.JSONObject;
import com.alibaba.fastjson.JSONObject;

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
	public static String create()
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
				System.out.println("teaResponseteaResponse---"+teaResponse.statusCode+"---teaResponse.statusMessage--"+teaResponse.statusMessage);
			}


		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}




	//绑定时间接口
	public static String active()
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

		}




		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}





	public static void main(String[] args) {
		create();
		//active();
	}

}
