package one.skean.booksalesinfo.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import one.skean.booksalesinfo.R;
import one.skean.booksalesinfo.baseclass.NetActivity;
import one.skean.booksalesinfo.bean.SaleCount;
import one.skean.booksalesinfo.bean.TopSale;
import one.skean.booksalesinfo.saxhandlers.TopSaleSAXHandler;
import one.skean.booksalesinfo.saxhandlers.SaleCountSAXHandler;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class WelcomeActivity extends NetActivity {
	private ListView lsvBookRanking, lsvSchoolRanking;
	private TextView txvTodayNum, txvTodayMoney;
	private ScrollView scvRoot;
	private Button btnMonthStatictis, btnQuery;

	private SaleCount todaySummary;
	private List<TopSale> todaySchoolRanking;
	private List<TopSale> todayBookRanking;

	private int timeCount = 0;
	private float currentNum = 0;
	private float currentMoney = 0.00f;
	private float targetNum;
	private float targetMoney;

	private static final int MSG_SUMMARY_SUCCESS = 3;
	private static final int MSG_ANIMATE_NUM = 4;
	private static final int MSG_BOOKRANKING_SUCCESS = 5;
	private static final int MSG_SCHOOLRANIKNG_SUCCESS = 6;

	private RankingAdapter lsvBookRankingAdapter;
	private RankingAdapter lsvSchoolRankingAdapter;
	private Handler mHandler;
	/* 数字更新动画效果的timertask */
	private Timer numAnimateTimer;
	/* 定时更新数据的Timer */
	private Timer refreshTimer;
	private OnClickListener btnListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_month_statictis:
				Intent intent = new Intent(getApplicationContext(), MonthActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_query:
				Intent intent2 = new Intent(getApplicationContext(), QueryActivity.class);
				startActivity(intent2);
				break;
			}

		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		Log.i("Skean", "Create");
		lsvBookRanking = (ListView) findViewById(R.id.lsv_book_ranking);
		lsvSchoolRanking = (ListView) findViewById(R.id.lsv_school_ranking);
		txvTodayNum = (TextView) findViewById(R.id.txv_todaynum);
		txvTodayMoney = (TextView) findViewById(R.id.txv_toadymoney);
		scvRoot = (ScrollView) findViewById(R.id.scv_root);
		btnMonthStatictis = (Button) findViewById(R.id.btn_month_statictis);
		btnQuery = (Button) findViewById(R.id.btn_query);
		btnQuery.setOnClickListener(btnListener);
		btnMonthStatictis.setOnClickListener(btnListener);
		mHandler = new Handler(this);
		lsvBookRankingAdapter = new RankingAdapter(new ArrayList<TopSale>());
		lsvSchoolRankingAdapter = new RankingAdapter(new ArrayList<TopSale>());
		lsvBookRanking.setAdapter(lsvBookRankingAdapter);
		lsvSchoolRanking.setAdapter(lsvSchoolRankingAdapter);
		/*
		 * 在scrollview嵌套listview将会出现奇怪问题:
		 * 第一次载入界面动态加载listview的项时候,将会跳转到listview的最后一项
		 * 使用下面的方法暂时结局了,载入后跳转回scrollview的头部
		 */
		scvRoot.post(new Runnable() {
			public void run() {
				scvRoot.scrollTo(0, 0);
			}
		});
		refreshTimer = new Timer();
		/* 每三十秒更新数据 */
		refreshTimer.schedule(new TimerTask() {
			public void run() {
				refreshData();
			}
		}, 0, 30000);
	}

	protected void onDestroy() {
		super.onDestroy();
		refreshTimer.cancel();
		Log.i("Skean", "Destroy");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("Skean", "Resume");
	}

	/* 更新数据的方法,开启三条线程获取数据 */
	private void refreshData() {
		/* 更新本日销售量金额的线程 */
		new Thread() {
			public void run() {
				SaleCountSAXHandler handler = new SaleCountSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "totalNumAndMoney");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String datesString = dateFormat.format(Calendar.getInstance().getTime());
				params.put("beginDate", datesString);
				params.put("endDate", datesString);
				todaySummary = (SaleCount) getWebData(params, handler);
				if (todaySummary != null) {
					try {
						targetNum = Float.parseFloat(todaySummary.getTotalnum());
						targetMoney = Float.parseFloat(todaySummary.getTotalmoney());
					}
					catch (Exception e) {
						Log.i("Skean", "获取总数失败!");
						e.printStackTrace();
					}
					// targetNum = targetNum + 11;
					// targetMoney = targetMoney + 134;
					mHandler.sendEmptyMessage(MSG_SUMMARY_SUCCESS);
				}
			}

		}.start();
		/* 更新学校销售的排名数据 */
		new Thread() {
			public void run() {
				TopSaleSAXHandler handler = new TopSaleSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "queryTop");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String datesString = dateFormat.format(Calendar.getInstance().getTime());
				params.put("beginDate", datesString);
				params.put("endDate", datesString);
				params.put("type", "school");
				params.put("startRow", "0");
				params.put("endRow", "5");
				todaySchoolRanking = (ArrayList<TopSale>) getWebData(params, handler);
				if (todaySchoolRanking != null && !todaySchoolRanking.isEmpty()) {
					lsvSchoolRankingAdapter.setRankingDatas(todaySchoolRanking);
					mHandler.sendEmptyMessage(MSG_SCHOOLRANIKNG_SUCCESS);
				}
			}
		}.start();
		/* 更新图书销售排行的数据 */
		new Thread() {
			public void run() {
				TopSaleSAXHandler handler = new TopSaleSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "queryTop");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String datesString = dateFormat.format(Calendar.getInstance().getTime());
				params.put("beginDate", datesString);
				params.put("endDate", datesString);
				params.put("type", "book");
				params.put("startRow", "0");
				params.put("endRow", "5");
				todayBookRanking = (ArrayList<TopSale>) getWebData(params, handler);
				if (todayBookRanking != null && !todayBookRanking.isEmpty()) {
					lsvBookRankingAdapter.setRankingDatas(todayBookRanking);
					mHandler.sendEmptyMessage(MSG_BOOKRANKING_SUCCESS);
				}
			}
		}.start();
	}

	public boolean handleMessage(Message msg) {

		switch (msg.what) {
		case MSG_SUMMARY_SUCCESS:
			/* 开始数据动画的timer */
			numAnimateTimer = new Timer();
			numAnimateTimer.schedule(new TimerTask() {
				public void run() {
					timeCount++;
					mHandler.sendEmptyMessage(MSG_ANIMATE_NUM);
					if (timeCount >= 30) {
						numAnimateTimer.cancel();
						timeCount = 0;
						currentNum = targetNum;
						currentMoney = targetMoney;
					}
				}
			}, 0, 50);
			break;
		case MSG_ANIMATE_NUM:
			txvTodayNum.setText(""
				+ Math.round(currentNum + (targetNum - currentNum) * timeCount / 30));
			txvTodayMoney.setText(String.format("%.2f", currentMoney + (targetMoney - currentMoney)
				* timeCount / 30));
			break;
		case MSG_BOOKRANKING_SUCCESS:
			lsvBookRankingAdapter.notifyDataSetChanged();
			break;
		case MSG_SCHOOLRANIKNG_SUCCESS:
			lsvSchoolRankingAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
		return false;
	}

	/* 列表的adapter */
	private class RankingAdapter extends BaseAdapter {
		private List<TopSale> rankingDatas;

		public RankingAdapter(List<TopSale> rankingDatas) {
			this.rankingDatas = rankingDatas;
		}

		public void setRankingDatas(List<TopSale> rankingDatas) {
			this.rankingDatas = rankingDatas;
		}

		public int getCount() {
			return rankingDatas.isEmpty() ? 0 : rankingDatas.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup root = (ViewGroup) getLayoutInflater()
				.inflate(R.layout.lsvitem_welcome, null);
			TextView titleTextView = (TextView) root.findViewById(R.id.lsvitem_ranking_titlepart);
			TextView numTextView = (TextView) root.findViewById(R.id.lsvitem_ranking_numpart);
			if (rankingDatas.size() > 0) {
				titleTextView.setText((position + 1) + ". " + rankingDatas.get(position).getName());
				switch (parent.getId()) {
				case R.id.lsv_book_ranking:
					numTextView.setTextColor(Color.rgb(153, 51, 204));
					numTextView.setText(rankingDatas.get(position).getNum() + "本");
					break;
				case R.id.lsv_school_ranking:
					numTextView.setText(rankingDatas.get(position).getMoney() + "元");
					break;
				default:
					break;
				}
			}
			return root;
		}

		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
