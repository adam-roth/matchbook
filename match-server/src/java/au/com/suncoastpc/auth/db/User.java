package au.com.suncoastpc.auth.db;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Collection;
import java.util.Collections;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import au.com.suncoastpc.auth.util.Base64;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.StringUtilities;
import au.com.suncoastpc.auth.util.types.UserState;
import au.com.suncoastpc.match.db.Game;

@Entity
@Table(name="users")
@NamedQueries({
	@NamedQuery(name="User.countAll", query="SELECT COUNT(*) FROM User u"),
	@NamedQuery(name="User.findAll", query="SELECT u FROM User u"),
	@NamedQuery(name="User.findByEmail", query="SELECT u FROM User u WHERE u.email = :email")
})
public class User implements Serializable {
	private static final Logger LOG = Logger.getLogger(User.class);
	/**
	 * Serialization id
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String email;
	private String firstName;
	private String lastName;
	private String hashedPassword;
	private String salt;
	private String authToken;
	private String formAuthToken;
	private UserState status;
	private int trustLevel;
	
	//relationships
	private Collection<Game> games;
	
	public User() {
		email = null;
		firstName = null;
		lastName = null;
		salt = null;
		hashedPassword = null;
		authToken = null;
		formAuthToken = null;
		trustLevel = Constants.TRUST_LEVEL_USER;	//everyone defaults to the standard user role
		status = UserState.STATE_INACTIVE;			//everyone defaults to inactive
		
		this.games = Collections.emptyList();
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@Column(unique=true)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
		if (this.email != null) {
			this.email = email.toLowerCase();
		}
	}
	
	@Column
	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	
	@Column
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	@Column
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	
	@Column
	public UserState getStatus() {
		return status;
	}
	public void setStatus(UserState status) {
		if (status == null) {
			status = UserState.STATE_INACTIVE;
		}
		this.status = status;
	}
	
	@Column
	public String getFormAuthToken() {
		return formAuthToken;
	}
	public void setFormAuthToken(String formAuthToken) {
		this.formAuthToken = formAuthToken;
	}
	
	@Column
	public int getTrustLevel() {
		return trustLevel;
	}
	public void setTrustLevel(int trustLevel) {
		this.trustLevel = trustLevel;
	}
	
	@Column(nullable = false)
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(nullable = false)
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@OneToMany(mappedBy = "creator")
	public Collection<Game> getGames() {
		return games;
	}
	public void setGames(Collection<Game> games) {
		this.games = games;
	}

	@Transient
	public String getName() {
		return StringUtilities.isEmpty(this.getFirstName()) ? this.getLastName() : this.getFirstName() + " " + this.getLastName();
	}
	
	@Transient
	public void setName(String name) {
		name = name.trim().replaceAll("[\\r\\t\\n]+", " ");  //convert all whitespace to space characters
		if (! name.contains(" ")) {
			//they only provided a single name, store it as their last name
			this.setFirstName("");
			this.setLastName(name);
		}
		else {
			//otherwise, everything up to the first space is used for the user's first name, and everything else is used for their last name
			String first = name.substring(0, name.indexOf(" "));
			String last = name.substring(name.indexOf(" ")).trim();
			this.setFirstName(first);
			this.setLastName(last);
		}
	}

	@Transient
	public void setPassword(String passwordPlaintext) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (this.getSalt() == null) {
			this.setSalt(StringUtilities.randomStringOfLength(16));
		}
		
		this.setHashedPassword(this.computeHash(passwordPlaintext, this.getSalt()));
	}
	
	@Transient
	public boolean checkPasswordForLogin(String passwordPlaintext) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (StringUtilities.isEmpty(passwordPlaintext)) {
			return false;
		}
		return this.getHashedPassword().equals(this.computeHash(passwordPlaintext, this.getSalt()));
	}
	
	@Transient
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		
		result.put("id", this.getId());
		result.put("email", this.getEmail());
		result.put("status", this.status.getName());
		result.put("trustLevel", this.getTrustLevel());
		result.put("authToken", this.getAuthToken());
		result.put("formAuthToken", this.getFormAuthToken());
		result.put("hashedPassword", this.getHashedPassword());
		result.put("salt", this.getSalt());
		
		return result;
	}
	
	@Transient
	public void generateNewAuthToken() {
		this.setAuthToken(StringUtilities.randomStringWithLengthBetween(16, 32));
	}
	
	@Transient
	public void generateNewFormAuthToken() {
		this.setFormAuthToken(StringUtilities.randomStringWithLengthBetween(16, 32));
	}
	
	@Transient
	public String getJsonString() {
		return this.toJson().toJSONString();
	}
	
	@Transient
	public boolean activateUserWithToken(String token) {
		if (token != null && token.equals(this.getAuthToken())) {
			this.setAuthToken(null);
			this.setStatus(UserState.STATE_CONFIRMED);			
			return true;
		}
		
		return false;
	}
	
	@Transient
	public boolean validateRequestWithToken(String formToken) {
		if (formToken != null && formToken.equals(this.getFormAuthToken())) {
			this.setFormAuthToken(null);
			return true;
		}
		
		return false;
	}
	
	@Transient
	private String computeHash(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), this.getSalt().getBytes(), 2048, 160);
		SecretKeyFactory fact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		
		LOG.info("Computing hash with password=?????????" + ", salt=" + salt + "; result=" + Base64.encodeBytes(fact.generateSecret(spec).getEncoded()));  //FIXME:  don't log the password in plaintext
		
		return Base64.encodeBytes(fact.generateSecret(spec).getEncoded());
	}
}
