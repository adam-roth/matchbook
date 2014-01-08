package au.com.suncoastpc.match.db;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.util.StringUtilities;

/**
 * A game represents a single app that is known to the system.  It associates an application bundle-id with 
 * a distinct id and a randomly-generated secret key that allows users to request matchmaking services 
 * for a given game.  Games are platform-agnostic.
 * 
 * Registered users can create and remove games.  In practice this should be something that the app developer does, 
 * similar to registering their app with GameCenter or OpenFeint.
 * 
 * @author Adam
 */
@Entity
@Table(name="games")
@NamedQueries({
	@NamedQuery(name="Game.findByAppAndPrivateKey", query="SELECT g FROM Game g WHERE g.appIdentifier = :app AND g.privateKey = :privateKey"),
	@NamedQuery(name="Game.findByCreatorAndApp", query="SELECT g FROM Game g WHERE g.creator = :creator AND g.appIdentifier = :app")
})
public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String appIdentifier;
	private String privateKey;
	
	//relationships
	private User creator;
	private Collection<MatchRequest> openRequests;
	
	public Game() {
		this.appIdentifier = null;
		this.privateKey = StringUtilities.randomStringOfLength(16);
		this.creator = null;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@Column(nullable = false)
	public String getAppIdentifier() {
		return appIdentifier;
	}
	public void setAppIdentifier(String appIdentifier) {
		this.appIdentifier = appIdentifier;
	}

	@Column(nullable = false)
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	@ManyToOne(optional = false)
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}

	@OneToMany(mappedBy = "game")
	public Collection<MatchRequest> getOpenRequests() {
		return openRequests;
	}
	public void setOpenRequests(Collection<MatchRequest> openRequests) {
		this.openRequests = openRequests;
	}
}
