package au.com.suncoastpc.auth.util.types;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum UserState {
	STATE_INACTIVE("Inactive"),
	STATE_CONFIRMED("Confirmed"),
	STATE_REQUESTED_PASSWORD_RESET("LostPass");
	
	private String name;
	
	private static final Map<String, UserState> STATES;
	
	static {
		STATES = new HashMap<String, UserState>();
		for (UserState state : EnumSet.allOf(UserState.class)) {
			STATES.put(state.getName(), state);
		}
	}
	
	private UserState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public UserState stateForName(String name) {
		return STATES.get(name);
	}

}
