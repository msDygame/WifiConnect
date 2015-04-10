package com.dygame.mywificonnect.wificonnecttest;
/* source code form eoeandroid chensit ; package com.chensir.activity; */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.util.Log;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity
{
    // 定義WifiManager對像
    private WifiManager mWifiManager;
    // 定義WifiInfo對像
    protected WifiInfo mWifiInfo;
    // 掃瞄出的網絡連接列表
    private List<ScanResult> mWifiListScanResult;
    // 網絡連接列表
    protected List<WifiConfiguration> mWifiConfiguration;
    // 定義一個WifiLock
    private ListView mWifiLsitView;
    ToggleButton mTurnwifi;
    wifiAdapeter adapter;
    private WifiBroadRecever mWifiBroadRecever;

    private String mLocalShow = "Connected";
    private TextView mConnectshow;
    private String TAG = "WifiConnectLog";
    private PopupWindow mShowCPopu = null;
    private View mShowCView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 取得WifiManager對像
        mWifiManager = (WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo對像
        mWifiInfo = mWifiManager.getConnectionInfo();
        String sWifi = (mWifiManager == null) ? "wifi is null" : "wifi ready" ;
        Log.i(TAG, "onCreate " + sWifi);
        initView();
        RegisterWifiRecever();
        refreshWifiList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getWifiListAndSetAdapter()
    {
        getWifiList();
        if (mWifiListScanResult != null)
        {
            adapter = new wifiAdapeter();
            mWifiLsitView.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mWifiBroadRecever);
    }

    private void pXspys()
    {
        getWifiList();
        if (mWifiListScanResult != null)
        {
            adapter.notifyDataSetChanged();
        }
        if (mWifiListScanResult == null)
        {
            mWifiListScanResult = new ArrayList<ScanResult>();
            adapter.notifyDataSetChanged();
        }
    }

    // /
    /**
     * 初始化界面
     */
    private void initView()
    {
        // TODO Auto-generated method stub
        mWifiLsitView = (ListView) findViewById(R.id.wifishowlist);
        mTurnwifi = (ToggleButton) findViewById(R.id.turnwifi);
        mConnectshow = (TextView) findViewById(R.id.turnwifi);
        mTurnwifi.setTextOff(getString(R.string.turnoffwifi));
        mTurnwifi.setTextOn(getString(R.string.turnonwifi));

        String sButtonState = (mTurnwifi == null) ? "Button is null" : "Button is ready";
        Log.i(TAG, "wifi initView " + sButtonState);
        String sListState = (mWifiLsitView == null) ? "List is null" : "List is ready";
        Log.i(TAG, "wifi initView " + sListState);
        if (mWifiManager.isWifiEnabled())
        {
            mTurnwifi.setChecked(true);
            getWifiListAndSetAdapter();
            Log.i(TAG, "wifi info" + getWifiInfo());
        }
        else
        {
            mTurnwifi.setChecked(false);
            Log.i(TAG, "wifi info check false");
        }
        mTurnwifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                // TODO Auto-generated method stub
                Log.i(TAG, "log make info:" + isChecked);
                if (isChecked)
                {
                    Log.i(TAG, "open wifi");
                    openWifi();
                }
                else
                {
                    if (mWifiListScanResult != null)
                    {
                        Log.i(TAG, "close wifi");
                        closeWifi();
                        pXspys();
                    }
                    Log.i(TAG, "onCheckChange but wifiList is null");
                }
            }
        });
        mWifiLsitView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                // TODO Auto-generated method stub
                int loac = arg2 - mWifiListScanResult.size();
                Log.i(TAG, "local position:" + arg2 + "long:" + arg3);

                if (loac < 0)
                {
                    Log.i(TAG, "local position:" + arg2);
                    ScanResult sr = mWifiListScanResult.get(arg2);

                    Log.i(TAG, "<----->" + sr.SSID);
                    WifiConfiguration c = checkWiFiConfig(sr.SSID);
                    if (c != null)
                    {
                        mWifiManager.enableNetwork(c.networkId, true);
                    }
                    else
                    {
                        // 如果沒有輸入密碼 且配置列表中沒有該WIFI
						/* WIFICIPHER_WPA 加密 */
                        if (sr.capabilities.contains("WPA-PSK"))
                        {
//							Log.i(TAG, "config----WPA-PSK");
                            showLoadingPop(sr.SSID);
                        }
                        else if (sr.capabilities.contains("WEP"))
                        {
							/* WIFICIPHER_WEP 加密 */
                            Log.i(TAG, "config----WEP");
                            int netid = mWifiManager.addNetwork(createWifiInfo(sr.SSID, "87654321", 2));
                            mWifiManager.enableNetwork(netid, true);
                        }
                        else
                        {
							/* WIFICIPHER_OPEN NOPASSWORD 開放無加密 */
                            int netid = mWifiManager.addNetwork(createWifiInfo(sr.SSID, "", 1));
                            mWifiManager.enableNetwork(netid, true);
                        }
                    }
                }
            }
        });
    }

    private void showLoadingPop(final String ssid)
    {
        Button btn_ok,btn_cancel;
        TextView title;
        final EditText password;
        if (mShowCView == null)
        {
            mShowCView = this.getLayoutInflater().inflate(R.layout.test1, null);
            mShowCView.setBackgroundResource(R.drawable.tv_show);
        }
        if (mShowCPopu == null)
        {
            mShowCPopu = new PopupWindow(mShowCView, ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
        }
        title=(TextView)mShowCView.findViewById(R.id.pop_title);
        password=(EditText)mShowCView.findViewById(R.id.pop_password);
        btn_ok=(Button)mShowCView.findViewById(R.id.pop_bt_ok);
        btn_cancel=(Button)mShowCView.findViewById(R.id.pop_bt_cancel);
        mShowCPopu.setFocusable(true);
        mShowCPopu.setOutsideTouchable(true);mShowCPopu.setBackgroundDrawable(new BitmapDrawable());
        title.setText(""+ssid);
        btn_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                String passwords=password.getText().toString().trim();
                if(passwords!=null&&!passwords.equals(""))
                {
                    int netid = mWifiManager.addNetwork(createWifiInfo(ssid, passwords, 3));
                    mWifiManager.enableNetwork(netid, true);
                    mShowCPopu.dismiss();
                    int i=isWifiContected(getApplicationContext());
                    Log.i("test", "locat :"+i);
                }
            }
        });
        mShowCPopu.showAtLocation(mShowCView, Gravity.CENTER, 0, 0);
    }

    private WifiConfiguration checkWiFiConfig(String str)
    {
        for (WifiConfiguration C : mWifiConfiguration)
        {
            if (str.equals(C.SSID))
            {
                return C;
            }
        }
        return null;
    }

    private void getWifiList()
    {
        // mWifiManager.;
        mWifiManager.startScan();
        // 得到掃瞄結果
        mWifiListScanResult = mWifiManager.getScanResults();
        // 得到配置好的網絡連接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    // 打開WIFI
    public void openWifi()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo()
    {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 關閉WIFI
    public void closeWifi()
    {
        if (mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(false);
        }
    }

    // 指定配置好的網絡進行連接
    public void connectConfiguration(int index)
    {
        // 索引大於配置好的網絡索引返回
        if (index > mWifiConfiguration.size())
        {
            return;
        }
        // 連接配置好的指定ID的網絡
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,true);
    }

    // 檢查當前WIFI狀態
    public int checkState()
    {
        return mWifiManager.getWifiState();
    }

    private void RegisterWifiRecever()
    {
        mWifiBroadRecever = new WifiBroadRecever();
        IntentFilter fileter = new IntentFilter();
        fileter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        fileter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        fileter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(this.mWifiBroadRecever, fileter);
    }

    protected static final int DEFINE_MESSAGE_WHAT = 10 ;
    private void refreshWifiList()
    {
        handler.removeMessages(DEFINE_MESSAGE_WHAT);
        handler.sendEmptyMessageDelayed(DEFINE_MESSAGE_WHAT, 10000);
        Log.i(TAG, "Handle Message push");
    }

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Log.i(TAG, "Handle Message income..");
            if (msg.what == DEFINE_MESSAGE_WHAT)
            {
                getWifiListAndSetAdapter();
                Log.i(TAG, "Handle Message income");
            }
        }
    };

    class wifiAdapeter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            // TODO Auto-generated method stub
            if (mWifiConfiguration != null && mWifiListScanResult != null)
            {
                return mWifiListScanResult.size() + mWifiConfiguration.size();
            }
            if (mWifiListScanResult != null)
                return mWifiListScanResult.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // TODO Auto-generated method stub
            int local = position - mWifiListScanResult.size();
            View v = getLayoutInflater().inflate(R.layout.list_item, null);
            ImageView img = (ImageView) v.findViewById(R.id.img);
            TextView tvTitle = (TextView) v.findViewById(R.id.title);
            TextView tvConnect = (TextView) v.findViewById(R.id.isconnect);
            // Wifi源名稱
            String currSsid = mWifiInfo.getSSID();
            if (local < 0)
            {
                ScanResult sr = mWifiListScanResult.get(position);
                String ssid = sr.SSID;
                tvTitle.setText(ssid);
                if (currSsid != null)
                {
                    if (ssid.contains(currSsid))
                    {
                        tvConnect.setText("Connected");
                    }
                }
                Log.i(TAG, "sr.capabilities:" + sr.capabilities);
                // Wifi的連接速度及信號強度：
                int numLevels = 5 ;//max=5,
                // 連接信號強度
                int iStrengthLevel = WifiManager.calculateSignalLevel(sr.level, numLevels) ;
                //current connect value:
                //取得 IpAddress ↓
                int ip = mWifiInfo.getIpAddress() ;
                // Convert little-endian to big-endianif needed
                if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
                {
                    ip = Integer.reverseBytes(ip);
                }
                byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
                String sIpAddressString;
                try
                {
                    sIpAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
                }
                catch (UnknownHostException ex)
                {
                    Log.e("WIFIIP", "Unable to get host address.");
                    sIpAddressString = null;
                }
                //連接信號強度
                int iConnectLevel = mWifiInfo.getRssi() ;
                //連接速度 , link speed unit -> Mbps
                int iSpeed = mWifiInfo.getLinkSpeed();
                Log.i(TAG, "WifiInfo ip:"+sIpAddressString);
                Log.i(TAG, "WifiInfo level:"+iConnectLevel);
                Log.i(TAG, "WifiInfo speed:"+iSpeed+"Mbps");
                /*實際執行結果:
                        title           Level       Strength
                        ----            -------     ----------
                        game01          -44     4
                        game05          -46     4
                        gameDLink     -59     3
                        game02          -62     3
                        game03          -72     2
                        game04          -80     1
                         //
                        if (Math.abs(sr.level) > 100)
                        {
                            img.setImageDrawable(getResources().getDrawable( R.drawable.wifi05));
                        }
                        else if (Math.abs(sr.level) > 70)
                        {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.wifi04));
                        }
                        else if (Math.abs(sr.level) > 60)
                        {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.wifi03));
                        }
                        else if (Math.abs(sr.level) > 50)
                        {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.wifi02));
                        }
                        else
                        {
                            img.setImageDrawable(getResources().getDrawable( R.drawable.wifi01));
                        }
                        */
                if (iStrengthLevel >= 4) img.setImageDrawable(getResources().getDrawable( R.drawable.wifi01));//full
                if (iStrengthLevel == 3) img.setImageDrawable(getResources().getDrawable( R.drawable.wifi02));//line third
                if (iStrengthLevel == 2) img.setImageDrawable(getResources().getDrawable( R.drawable.wifi03));//line two
                if (iStrengthLevel == 1) img.setImageDrawable(getResources().getDrawable( R.drawable.wifi04));//line one
                if (iStrengthLevel == 0) img.setImageDrawable(getResources().getDrawable( R.drawable.wifi05));//zero
                return v;
            }
            else
            {
                WifiConfiguration cf = mWifiConfiguration.get(local);
                String ssid = cf.SSID;

                if (currSsid != null && ssid != null)
                {
                    if (ssid.contains(currSsid))
                    {
                        return v;
                    }
                }
                if (ssid != null) tvTitle.setText(ssid);
                tvConnect.setText("Out of Range");//不在範圍內//Not within the scope?
                return v;
            }
        }
    }

    public static final int DEFINE_WIFI_CONNECTED = 0x01;
    public static final int DEFINE_WIFI_CONNECT_FAILED = 0x02;
    public static final int DEFINE_WIFI_CONNECTING = 0x03;
    public int isWifiContected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        Log.v(TAG, "isConnectedOrConnecting = " + wifiNetworkInfo.isConnectedOrConnecting());
        Log.d(TAG, "wifiNetworkInfo.getDetailedState() = " + wifiNetworkInfo.getDetailedState());
        if (wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR || wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING)
        {
            return DEFINE_WIFI_CONNECTING;
        }
        else if (wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED)
        {
            return DEFINE_WIFI_CONNECTED;
        }
        else
        {
            Log.d(TAG, "getDetailedState() == " + wifiNetworkInfo.getDetailedState());
            return DEFINE_WIFI_CONNECT_FAILED;
        }
    }

    private WifiConfiguration IsExsits(String SSID)
    {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\"" + SSID + "\"") )
            {
                return existingConfig;
            }
        }
        return null;
    }

    class WifiBroadRecever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(WifiManager.ACTION_PICK_WIFI_NETWORK))
            {

            }
            else if (intent.getAction().equals( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                getWifiListAndSetAdapter() ;//更新太頻繁...
            }

            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION))
            {
                Log.d(TAG, "RSSI changed");

                // 有可能是正在獲取，或者已經獲取了
                Log.d(TAG, " intent is " + WifiManager.RSSI_CHANGED_ACTION);

                if (isWifiContected(getApplicationContext()) == DEFINE_WIFI_CONNECTED)
                {
                    mWifiInfo = mWifiManager.getConnectionInfo();
                    mLocalShow = "Connected";
                    mConnectshow.setText(mWifiInfo.getSSID() + ":" + mLocalShow);

                }
                else if (isWifiContected(getApplicationContext()) == DEFINE_WIFI_CONNECT_FAILED)
                {
                    closeWifi();
                }
                else if (isWifiContected(getApplicationContext()) == DEFINE_WIFI_CONNECTING)
                {
                    mLocalShow = "Connecting..";
                    mConnectshow.setText(mLocalShow);
                }
            }
        }
    }

    public WifiConfiguration createWifiInfo(String SSID, String Password,int Type)
    {
        Log.v(TAG, "SSID = " + SSID + "## Password = " + Password + "## Type = " + Type);
        if (SSID == null || Password == null || SSID.equals(""))
        {
            Log.e(TAG, "addNetwork() ## nullpointer error!");
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExsits(SSID, mWifiManager);
        if (tempConfig != null)
        {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        //定義幾種加密方式，一種是WEP，一種是WPA，還有沒有密碼的情況，public enum WifiCipherType...偷懶...用int吧...
        if (Type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 判斷wifi是否存在
     *
     * @param SSID
     * @param wifiManager
     * @return
     */
    private static WifiConfiguration isExsits(String SSID, WifiManager wifiManager)
    {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\"" + SSID + "\""))
            {
                return existingConfig;
            }
        }
        return null;
    }
}