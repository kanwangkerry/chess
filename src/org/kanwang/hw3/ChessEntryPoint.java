package org.kanwang.hw3;

import org.kanwang.hw6.Match;
import org.kanwang.hw6.client.LoginInfo;
import org.kanwang.hw6.client.GameService;
import org.kanwang.hw6.client.GameServiceAsync;
import org.kanwang.hw8.ChessConstants;
import org.kanwang.hw8.ChessMessages;
import org.shared.chess.Move;
import org.shared.chess.State;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactoryImpl;
import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ChessEntryPoint implements EntryPoint {

	final Presenter presenter = new Presenter();
	private LoginInfo loginInfo = null;

	private ChessConstants constants = GWT.create(ChessConstants.class);
	private ChessMessages messages = GWT.create(ChessMessages.class);

	private void loadLogin() {
		VerticalPanel loginPanel = new VerticalPanel();
		Label loginLabel = new Label(constants.loginInfo());
		Anchor signInLink = new Anchor(constants.logoutAnchor());
		// Assemble login panel.
		signInLink.setHref(loginInfo.getLoginUrl());
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		RootPanel.get().add(loginPanel);
	}

	private void loadChannelService() {
		// channel for the game.
		presenter.setUser(loginInfo);
		Channel gameChannel = new ChannelFactoryImpl().createChannel(loginInfo
				.getToken());
		Socket socket = gameChannel.open(new SocketListener() {
			@Override
			public void onOpen() {
			}

			@Override
			public void onMessage(String message) {
				if (message.startsWith("*")) {
					long mID = Long.parseLong(message.substring(1));
					GameServiceAsync game = GWT.create(GameService.class);
					game.getMatchWithID(mID + "", new AsyncCallback<Match>() {
						public void onFailure(Throwable error) {
						}

						@Override
						public void onSuccess(Match result) {
							if (result == null)
								Window.alert(constants.alertWrongMatchInfo());
							try {
								presenter.state = Presenter
										.getStateFromString(result.getState());
								presenter.setMatch(result);
								presenter.init_state();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					presenter.setMatchInfo();

				} else if (message.startsWith("&")) {
					presenter.setMatchInfo();
				} else if (message.startsWith("!")){
					presenter.setRatingInfo(Integer.parseInt(message.substring(1)));
				} 
				else {
					String temp[] = message.split(" ");
					if (Long.parseLong(temp[1]) != presenter.getMatch().getID()) {
						Window.alert(messages.alertUpdate(temp[1]));
						return;
					}
					Move move = Presenter.stringToMove(temp[0]);
					presenter.setMovePromotion(move.getPromoteToPiece());
					presenter.makeMoveStateAndView(move.getFrom(), move.getTo());
					GameServiceAsync game = GWT.create(GameService.class);
					game.getMatchWithID(presenter.getMatch().getID() + "",
							new AsyncCallback<Match>() {
								public void onFailure(Throwable error) {
								}

								@Override
								public void onSuccess(Match result) {
									if (result == null)
										Window.alert(constants
												.alertWrongMatchInfo());
									try {
										presenter.state = Presenter
												.getStateFromString(result
														.getState());
										presenter.setMatch(result);
										presenter.setMatchInfo();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
				}
			}

			@Override
			public void onError(ChannelError error) {
				Window.alert("Error: " + error.getDescription());
			}

			@Override
			public void onClose() {
				Window.alert(constants.alertChannelClose());
			}
		});

	}

	private void loadChessView() {
		// create channel
		loadChannelService();

		// set view
		final Graphics graphics = new Graphics(presenter, loginInfo);
		graphics.user = loginInfo;
		presenter.setView(graphics);
		try {
			presenter.state = Presenter.getStateFromString(History.getToken())
					.copy();
		} catch (Exception e1) {
			presenter.state = new State();
		}
		presenter.setMatchInfo();
		RootPanel.get().add(graphics);

		// set history
		// History.addValueChangeHandler(new ValueChangeHandler<String>() {
		// @Override
		// public void onValueChange(ValueChangeEvent<String> event) {
		// String historyToken = event.getValue();
		// // Parse the history token
		// try {
		// presenter.state = Presenter
		// .getStateFromString(historyToken).copy();
		// graphics.init();
		// } catch (Exception e) {
		// System.out.println(historyToken);
		// graphics.init();
		// }
		// }
		// });

	}

	@Override
	public void onModuleLoad() {

		GameServiceAsync loginService = GWT.create(GameService.class);
		loginService.login(GWT.getHostPageBaseURL() + "kanwang.html",
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
					}

					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						if (loginInfo.isLoggedIn()) {
							loadChessView();
						} else {
							loadLogin();
						}
					}
				});
	}

}
