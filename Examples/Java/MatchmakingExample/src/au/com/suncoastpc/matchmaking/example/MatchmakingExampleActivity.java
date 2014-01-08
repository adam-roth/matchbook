package au.com.suncoastpc.matchmaking.example;

import org.json.simple.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.com.suncoastpc.match.api.Match;
import au.com.suncoastpc.match.api.Matchmaker;
import au.com.suncoastpc.match.api.MatchmakerClient;

public class MatchmakingExampleActivity extends Activity implements MatchmakerClient {
	private static final String APP_ID = "au.com.suncoastpc.MatchmakingExample";
	private static final String API_KEY = "ef20a2a065e345d0";
	private static final int NUM_PLAYERS = 4;
	
	private Matchmaker matchmaker;
	private Match match;
	private View background;
	private LinearLayout layout;
	
	private int counter;
	private boolean matchRunning;
	private TextView countView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        counter = 0;
        matchRunning = false;
        
        layout = this.getCurrentLayout();
        setContentView(layout);
        
        background = layout;//this.findViewById(R.id.backgroundView);
        background.setBackgroundColor(Color.RED);
        
        TelephonyManager manager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        matchmaker = new AndroidMatchmaker(manager, APP_ID, API_KEY, this);
        
        countView = new TextView(this);
        countView.setText(Integer.toString(counter));
        countView.setBackgroundColor(Color.TRANSPARENT);
        countView.setTextColor(Color.WHITE);
        countView.setTypeface(Typeface.DEFAULT_BOLD);
        countView.setTextSize(64);
        countView.setVisibility(View.GONE);
        countView.setGravity(Gravity.CENTER);
        
        //handling taps on the counter view
        countView.setOnClickListener(new OnClickListener() {
        	@SuppressWarnings("unchecked")
			@Override
        	public void onClick(View view) {
        		if (match != null && matchRunning) {
        			synchronized(countView) {
        				counter++;
        				updateCounter();
        			}
        			
        			JSONObject message = new JSONObject();
        			message.put("message", "increment");
        			match.broadcastMessage(message);
        		}
        	}
        });
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		layout.addView(countView, params);
        
        this.setupMatch();
    }
    
    @Override
    protected void onDestroy() {
    	if (match != null) {
    		matchmaker.cancelMatch(match);
    	}
    	super.onDestroy();
    }

    //matchmaking API implementation
	@Override
	public void dataReceivedFromPlayer(String playerId, JSONObject data) {
		// TODO Auto-generated method stub
		if ("PING!".equals(data.get("message"))) {
			this.respondToPing(playerId);
			matchRunning = true;
			this.updateCounter();
		}
		else if ("increment".equals(data.get("message"))) {
			synchronized(countView) {
				counter++;
				this.updateCounter();
			}
		}
		else {
			Long color = (Long)data.get("color");
			this.updateLabelForPlayer(playerId, color);
			matchRunning = true;
			this.updateCounter();
		}
	}

	@Override
	public JSONObject getPlayerDetails() {
		return null;
	}

	@Override
	public void playerJoined(String playerId, JSONObject playerInfo) {
		this.addLabelForPlayer(playerId);
		if (match != null && match.amITheServer()) {
			if (match.getPlayers().size() == NUM_PLAYERS - 2) {
				this.startMatch();
			}
		}
	}

	@Override
	public void playerLeft(String playerId) {
		this.removeLabelForPlayer(playerId);
	}

	@Override
	public boolean shouldAcceptJoinFromPlayer(String playerId, JSONObject playerDetails) {
		return match.getPlayers().size() < NUM_PLAYERS - 1;
	}
	
	private void updateCounter() {
		if (match != null && matchRunning) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					countView.setVisibility(View.VISIBLE);
					countView.setText(Integer.toString(counter));
				}
			});
		}
	}
	
	//helper functions
	private int randomRGB() {
    	return (int)(Math.random() * 256);
    }
	
	private void setupMatch() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
		        	Thread.sleep(3000);
		        }
		        catch (Exception ignored) { }
		        
		        match = matchmaker.autoJoinMatch(NUM_PLAYERS, true);
		        
		        runOnUiThread(new Runnable() {
		        	@Override
		        	public void run() {
		        		if (match != null) {
				        	background.setBackgroundColor(Color.BLUE);
				        }
				        else {
				        	background.setBackgroundColor(Color.BLACK);
				        }
		        	}
		        });
			}
		}).start();
	}
	
	private void startMatch() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				matchmaker.startMatch(match);
				background.setBackgroundColor(Color.GREEN);
				new PingThread(match).start();
			}
		});
	}
	
	private void respondToPing(final String playerId) {
		this.runOnUiThread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				int red = randomRGB();
				int green = randomRGB();
				int blue = randomRGB();
				
				//change our background color
				int color = 0xFF000000 | (red << 16) | (green << 8) | blue;
				background.setBackgroundColor(color);
				
				//send the new color back to the server
				JSONObject response = new JSONObject();
				response.put("color", color);
				//match.sendMessageToPlayer(response, playerId);
				match.broadcastMessage(response);
			}
		});
	}
	
	private LinearLayout getCurrentLayout() {
		LayoutInflater inflater = getLayoutInflater();
		return (LinearLayout) inflater.inflate(R.layout.main,null);
	}
	
	private void addLabelForPlayer(final String playerId, final int playerIndex) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView label = new TextView(MatchmakingExampleActivity.this);
				label.setPadding(10, 15, 0, 15);
				label.setText("Player " + playerIndex + ":  " + playerId);
				label.setBackgroundColor(Color.BLACK);
				label.setTextColor(Color.BLUE);
				label.setTag(playerId);
				label.setTypeface(Typeface.DEFAULT_BOLD);
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				
				layout.removeView(countView);
				layout.addView(label, params);
				layout.addView(countView);  //countView should always be last
			}
		});
	}
	
	private void addLabelForPlayer(final String playerId) {
		this.addLabelForPlayer(playerId, (match == null ? 0 : match.getPlayers().size()));
	}
	
	private void updateLabelForPlayer(final String playerId, final Long color) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView label = (TextView)layout.findViewWithTag(playerId);
				label.setTextColor(0xFF000000 | color.intValue());
			}
		});
	}
	
	private void removeLabelForPlayer(final String playerId) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//remove the view
				TextView label = (TextView)layout.findViewWithTag(playerId);
				if (label != null) {
					layout.removeView(label);
				}
				
				//reposition any other views
				if (match != null) {
					int numLabels = 0;
					for (String player : match.getPlayers()) {
						label = (TextView)layout.findViewWithTag(player);
						if (label != null) {
							layout.removeView(label);
						}
						if (! player.equals(playerId)) {
							addLabelForPlayer(player, numLabels);
							numLabels++;
						}
					}
				}
			}
		});
	}
	
	//server thread
	private static class PingThread extends Thread {
		private Match match;
		
		public PingThread(Match match) {
			this.match = match;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			JSONObject pingMessage = new JSONObject();
			pingMessage.put("message", "PING!");
			
			while (match != null && match.getPlayers().size() > 0) {
				try {
					Thread.sleep(5000);
					match.broadcastMessage(pingMessage);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}