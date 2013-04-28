package org.kanwang.hw6.server;

import java.util.LinkedList;

import org.kanwang.hw2.StateChangerImpl;
import org.kanwang.hw3.Presenter;
import org.kanwang.hw6.Match;
import org.kanwang.hw6.Player;
import org.kanwang.hw6.client.LoginInfo;
import org.kanwang.hw6.client.GameService;
import org.shared.chess.Color;
import org.shared.chess.Move;
import org.shared.chess.State;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

@SuppressWarnings("serial")
public class GameServiceImpl extends RemoteServiceServlet implements
		GameService {
	static {
		ObjectifyService.register(Match.class);
		ObjectifyService.register(Player.class);
	}

	public LoginInfo login(String requestUri) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		LoginInfo loginInfo = new LoginInfo();

		if (user != null) {
			loginInfo.setLoggedIn(true);
			loginInfo.setEmailAddress(user.getEmail());
			loginInfo.setNickname(user.getNickname());
			loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));

			// create the channel service
			ChannelService channelService = ChannelServiceFactory
					.getChannelService();
			String token = channelService.createChannel(loginInfo
					.getEmailAddress());
			loginInfo.setToken(token);

			// Create a user
			Player p = this.getPlayer(loginInfo.getEmailAddress());
			if (p == null) {
				p = new Player(loginInfo);
				p.setToken(token);
				Objectify ofy = ObjectifyService.ofy();
				ofy.save().entity(p).now();
			}

			// register a match

			// for (Match m : matches) {
			// if (m.getB() == null) {
			// m.setB(loginInfo.getEmailAddress());
			// loginInfo.setMatch(m.getID());
			// String tempMessage1 = "*"+loginInfo.getEmailAddress();
			// channelService.sendMessage(new ChannelMessage(m
			// .getAnotherUser(loginInfo.getEmailAddress()),
			// tempMessage1));
			// break;
			// }
			// if (m.getW() == null) {
			// m.setW(loginInfo.getEmailAddress());
			// loginInfo.setMatch(m.getID());
			// break;
			// }
			// }
			// if (loginInfo.getMatch() == 0) {
			// Match m = new Match(new State());
			// m.setW(loginInfo.getEmailAddress());
			// loginInfo.setMatch(m.getID());
			// matches.add(m);
			// Objectify ofy= ObjectifyService.ofy();
			// ofy.save().entity(m).now();
			// }

		} else {
			loginInfo.setLoggedIn(false);
			loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
		}
		return loginInfo;
	}

	@Override
	public Match submitMove(String move, Match m, String user) {
		try {
			Objectify ofy = ObjectifyService.ofy().transaction();
			// load data from server
			Player p = ofy.load().type(Player.class).id(user).get();
			Match mr = ofy.load().entity(m).get();
			// check if legal
			if (!mr.isUserTurn(p.getID())) {
				return null;
			}
			// make move
			State s = Presenter.getStateFromString(mr.getState());
			StateChangerImpl sc = new StateChangerImpl();
			Move move1 = Presenter.stringToMove(move);
			sc.makeMove(s, move1);
			mr.setState(Presenter.makeStringfromState(s));
			m.setState(Presenter.makeStringfromState(s));
			ofy.save().entity(mr).now();
			ofy.getTxn().commit();
			// send this move
			ChannelService channelService = ChannelServiceFactory
					.getChannelService();
			channelService.sendMessage(new ChannelMessage(m
					.getAnotherUser(user), move + " " + m.getID()));
			channelService.sendMessage(new ChannelMessage(user, move + " "
					+ m.getID()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return m;
	}

	@Override
	public LinkedList<Match> getMatches(String user) {
		LinkedList<Match> result = new LinkedList<Match>();
		Objectify ofy = ObjectifyService.ofy();
		Iterable<Match> list = ofy.load().type(Match.class);
		for (Match mt : list) {
			if (mt.getB().equals(user) || mt.getW().equals(user))
				result.add(mt);
		}
		return result;
	}

	@Override
	public Match createMatch(String w, String b) {
		Objectify ofy = ObjectifyService.ofy();
		Iterable<Player> list = ofy.load().type(Player.class);
		Player x = null;
		for (Player p : list) {
			if (p.getID().equals(b))
				x = p;
		}
		if (x == null)
			return null;
		Match m = new Match(new State());
		m.setW(w);
		m.setB(b);
		ofy = ObjectifyService.ofy().transaction();
		ofy.save().entity(m).now();
		ofy.getTxn().commit();

		ChannelService channelService = ChannelServiceFactory
				.getChannelService();
		channelService.sendMessage(new ChannelMessage(b, "*" + m.getID()));
		return m;
	}

	@Override
	public Match getMatchWithID(String matchID) {
		Objectify ofy = ObjectifyService.ofy();
		Iterable<Match> list = ofy.load().type(Match.class);
		long id = Long.parseLong(matchID);
		for (Match m : list) {
			if (m.getID() == id)
				return m;
		}
		return null;
	}

	@Override
	public Match deleteMatch(long matchID, String user) {
		Objectify ofy = ObjectifyService.ofy();
		Iterable<Match> list = ofy.load().type(Match.class);
		Match x = null;
		for (Match m : list) {
			if (m.getID() == matchID) {
				x = m;
				break;
			}
		}
		if (x == null)
			return x;
		if (x.shouldDelete(user)) {
			ofy = ObjectifyService.ofy().transaction();
			ofy.delete().entity(x).now();
			ofy.getTxn().commit();
		} else {
			x.setDeleted(user);
			ofy = ObjectifyService.ofy().transaction();
			ofy.save().entity(x).now();
			ofy.getTxn().commit();
		}
		return x;

	}

	@Override
	public Match autoCreate(String user) {
		Objectify ofy = ObjectifyService.ofy();
		Iterable<Player> list = ofy.load().type(Player.class);
		Player x = null;
		for (Player p : list) {
			if (p.isWaiting()) {
				x = p;
				break;
			}
		}
		if (x == null) {
			for (Player p : list) {
				if (p.getID().equals(user)) {
					x = p;
					break;
				}
			}
			x.setWaiting(true);
			ofy.save().entity(x);
			return null;
		}
		x.setWaiting(false);
		ofy.save().entity(x);
		return this.createMatch(user, x.getID());
	}

	@Override
	public Player updateRating(String matchID) {
		Objectify ofy = ObjectifyService.ofy();
		Match m = this.getMatchWithID(matchID);
		if (m == null)
			return null;
		Iterable<Player> list = ofy.load().type(Player.class);
		Player w = null;
		Player b = null;
		for (Player p : list) {
			if (p.getID().equals(m.getW())) {
				w = p;
			}
			if (p.getID().equals(m.getB())) {
				b = p;
			}
		}
		State state;
		try {
			state = Presenter.getStateFromString(m.getState());
			// calc rating
			Player oppo = w;
			Player cur = b;
			int resultB = 0;
			double e = 1 / (1 + Math.pow(10,
					(oppo.getRating() - cur.getRating()) / ((double) 400)));
			double s;
			if (state.getGameResult().isDraw())
				s = 0.5;
			else if (state.getGameResult().getWinner() == Color.BLACK)
				s = 1;
			else
				s = 0;
			resultB = (int) (cur.getRating() + 15 * (s - e));

			oppo = b;
			cur = w;
			int resultW = 0;
			e = 1 / (1 + Math.pow(10, (oppo.getRating() - cur.getRating())
					/ ((double) 400)));
			if (state.getGameResult().isDraw())
				s = 0.5;
			else if (state.getGameResult().getWinner() == Color.WHITE)
				s = 1;
			else
				s = 0;
			resultW = (int) (cur.getRating() + 15 * (s - e));

			w.setRating(resultW);
			b.setRating(resultB);
			ofy.save().entity(w);
			ofy.save().entity(b);
			// commit rating;
			ChannelService channelService = ChannelServiceFactory
					.getChannelService();
			channelService.sendMessage(new ChannelMessage(b.getID(), "!"
					+ b.getRating()));
			channelService.sendMessage(new ChannelMessage(w.getID(), "!"
					+ w.getRating()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return null;
	}

	@Override
	public Player getPlayer(String userID) {
		Objectify ofy = ObjectifyService.ofy();
		Iterable<Player> list = ofy.load().type(Player.class);
		for (Player p : list) {
			if (p.getID().equals(userID)) {
				return p;
			}
		}
		return null;
	}
}