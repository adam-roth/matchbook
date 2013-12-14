package au.com.suncoastpc.match.spring;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import au.com.suncoastpc.auth.annotations.RequiresLogin;
import au.com.suncoastpc.auth.annotations.RequiresParameters;
import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.spring.base.BaseMethods;
import au.com.suncoastpc.auth.util.AccessUtils;
import au.com.suncoastpc.match.db.Game;
import au.com.suncoastpc.match.db.GameDAO;

@RequiresLogin
public class UserMethods extends BaseMethods {
	//private static final Logger LOG = Logger.getLogger(UserMethods.class);
	
	@RequiresParameters(paramNames = {"name"}, redirectTo = "index")
	public ModelAndView addGame(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = AccessUtils.getCurrentUser(request);
		String app = request.getParameter("name");
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		Game existing = GameDAO.findByCreatorAndApp(user, app, em);
		if (existing == null) {
			em.getTransaction().begin();
			
			existing = new Game();
			existing.setCreator(user);
			existing.setAppIdentifier(app);
			
			em.persist(existing);
			em.getTransaction().commit();
		}
		else {
			request.setAttribute("error", "You already have a game named " + app + "!");
		}
		
		return indexPage(request, response);
	}
}
