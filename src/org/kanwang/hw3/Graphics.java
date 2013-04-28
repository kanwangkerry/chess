package org.kanwang.hw3;

import java.util.LinkedList;

import org.shared.chess.Color;
import org.shared.chess.GameResult;
import org.shared.chess.Piece;
import org.shared.chess.State;
import org.kanwang.hw3.Presenter.View;
import org.kanwang.hw6.client.GameService;
import org.kanwang.hw6.client.GameServiceAsync;
import org.kanwang.hw6.client.LoginInfo;
import org.kanwang.hw6.Match;
import org.kanwang.hw6.Player;
import org.kanwang.hw8.ChessConstants;
import org.kanwang.hw8.ChessMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.storage.client.Storage;

public class Graphics extends Composite implements View {
	private static GameImages gameImages = GWT.create(GameImages.class);
	private static GraphicsUiBinder uiBinder = GWT
			.create(GraphicsUiBinder.class);

	interface GraphicsUiBinder extends UiBinder<Widget, Graphics> {
	}

	@UiField
	GameCss css;
	@UiField
	Label gameStatus;
	@UiField
	Grid gameGrid;
	@UiField
	Grid promotionSelect;
	@UiField
	SimplePanel moveContainer;
	@UiField
	Button english, chinese;
	// @UiField
	// Button restart;
	@UiField
	HorizontalPanel userPanel;
	@UiField
	HorizontalPanel matchPanel;
	@UiField
	HorizontalPanel matchInfoPanel;

	private AbsolutePanel boardContainer[][] = new AbsolutePanel[8][8];

	private Image[][] board = new Image[8][8];
	private Image[][] pieceImage = new Image[8][8];
	private Image[] promotion = new Image[4];

	private final Presenter presenter;
	// record if the board is already released. If it is released by
	// showEnd or Selected, it should not be unRelease by highlight
	private boolean isReleased[][] = new boolean[8][8];

	Image moveImage;

	double gridTop, gridLeft;

	HandlerRegistration removeHandler;

	private boolean useAnimation = true;
	private boolean useDragDrop = DragDropEventBase.isSupported();

	Audio moveAudio = Audio.createIfSupported();
	Audio overAudio = Audio.createIfSupported();

	Storage store = Storage.getLocalStorageIfSupported();

	LoginInfo user;

	private Label userStatus = new Label();
	private ListBox matchInfo = new ListBox();
	private Label matchDate = new Label();
	private Label ratingInfo = new Label();

	private ChessConstants constants = GWT.create(ChessConstants.class);
	private ChessMessages messages = GWT.create(ChessMessages.class);

	public Graphics(Presenter p, LoginInfo login) {

		presenter = p;
		initWidget(uiBinder.createAndBindUi(this));
		gameGrid.resize(8, 8);
		gameGrid.setCellPadding(0);
		gameGrid.setCellSpacing(0);
		gameGrid.setBorderWidth(0);
		gridTop = 150;
		gridLeft = Window.getClientWidth() / 2 - 45 * 4;
		gameGrid.getElement().getStyle().setTop(gridTop, Unit.PX);
		gameGrid.getElement().getStyle().setPosition(Position.ABSOLUTE);
		gameGrid.getElement().getStyle().setLeft(gridLeft, Unit.PX);

		gameStatus.getElement().getStyle().setFloat(Style.Float.LEFT);
		gameStatus.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
		gameStatus.getElement().getStyle().setFontSize(20, Unit.PX);
		gameStatus.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		gameStatus.setText(constants.gameNotBegin());

		// set log out
		Anchor logOut = new Anchor(constants.logoutAnchor());

		user = login;
		userStatus.getElement().getStyle().setFloat(Style.Float.LEFT);
		userStatus.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
		userStatus.getElement().getStyle().setFontSize(16, Unit.PX);
		userStatus.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		userStatus.setText(user.getEmailAddress());

		matchDate.getElement().getStyle().setFloat(Style.Float.RIGHT);
		matchDate.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
		matchDate.getElement().getStyle().setFontSize(16, Unit.PX);
		matchDate.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		matchDate.setText(constants.gameNotBegin());

		logOut.setHref(user.getLogoutUrl());
		logOut.getElement().getStyle().setFontSize(16, Unit.PX);
		logOut.getElement().getStyle().setFloat(Style.Float.RIGHT);
		logOut.getElement().getStyle().setPosition(Position.RELATIVE);
		
		ratingInfo.getElement().getStyle().setFloat(Style.Float.LEFT);
		ratingInfo.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
		ratingInfo.getElement().getStyle().setFontSize(16, Unit.PX);
		ratingInfo.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		ratingInfo.getElement().getStyle().setMarginLeft(0.8, Unit.CM);
		ratingInfo.setText(messages.labelRating(1500+""));
		GameServiceAsync game = GWT.create(GameService.class);
		game.getPlayer(user.getEmailAddress(), new AsyncCallback<Player>(){
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Player result) {
				ratingInfo.setText(messages.labelRating(result.getRating()+""));
			}});
		

		// Make enough room for all five items (setting this value to 1 turns it
		// into a drop-down list).
		matchInfo.setVisibleItemCount(1);
		matchInfo.getElement().getStyle().setFontSize(16, Unit.PX);
		matchInfo.getElement().getStyle().setFloat(Style.Float.RIGHT);
		matchInfo.getElement().getStyle().setPosition(Position.RELATIVE);
		matchInfo.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int index = matchInfo.getSelectedIndex();
				String s = matchInfo.getItemText(index);
				String id;
				if (s.startsWith("游戏")) {
					id = s.substring(2, s.indexOf("："));
				} else {
					String temp[] = s.split(" ");
					id = temp[0].substring(0, temp[0].length() - 1);
				}
				GameServiceAsync game = GWT.create(GameService.class);
				game.getMatchWithID(id, new AsyncCallback<Match>() {
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
							presenter.setMatchInfo();
							init();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

		});

		userPanel.add(userStatus);
		userPanel.add(ratingInfo);
		userPanel.add(logOut);
		matchInfoPanel.add(matchInfo);
		matchInfoPanel.add(matchDate);

		init_promotion();
		init_matchPanel();
		promotionSelect.setVisible(false);

		// restart.setText("Restart!");
		// restart.getElement().getStyle().setFloat(Style.Float.RIGHT);
		// restart.getElement().getStyle().setPosition(Position.RELATIVE);
		// restart.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// presenter.state = new State();
		// presenter.setState(presenter.state);
		// }
		// });
		//
		english.setText(constants.langEnglish());
		english.getElement().getStyle().setFloat(Style.Float.RIGHT);
		english.getElement().getStyle().setPosition(Position.RELATIVE);
		english.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open(GWT.getHostPageBaseURL() + "kanwang.html", "", "");
			}
		});

		chinese.setText(constants.langChinese());
		chinese.getElement().getStyle().setFloat(Style.Float.RIGHT);
		chinese.getElement().getStyle().setPosition(Position.RELATIVE);
		chinese.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open(
						GWT.getHostPageBaseURL() + "kanwang.html?locale=zh",
						"", "");
			}
		});

		// load.setText("Load");
		// load.getElement().getStyle().setFloat(Style.Float.RIGHT);
		// load.getElement().getStyle().setPosition(Position.RELATIVE);
		// load.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// presenter.state = presenter.loadState().copy();
		// presenter.init_state();
		// }
		// });
		// if (!Storage.isSupported()) {
		// save.setVisible(false);
		// load.setVisible(false);
		// }

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				board[row][col] = new Image();
				if (row % 2 == 0 && col % 2 == 1 || row % 2 == 1
						&& col % 2 == 0) {
					board[row][col].setResource(gameImages.blackTile());
				} else {
					board[row][col].setResource(gameImages.whiteTile());
				}
				final int i = row;
				final int j = col;
				board[row][col].addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						presenter.overCell(i, j);
					}
				});

				board[row][col].addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						presenter.outCell(i, j);
					}
				});

				board[row][col].addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						presenter.selectPiece(i, j);
					}
				});

				if (this.useDragDrop) {
					board[row][col].addDragOverHandler(new DragOverHandler() {
						@Override
						public void onDragOver(DragOverEvent event) {
							presenter.overCell(i, j);
						}
					});
					board[row][col].addDragLeaveHandler(new DragLeaveHandler() {
						@Override
						public void onDragLeave(DragLeaveEvent event) {
							presenter.outCell(i, j);
						}
					});
					board[row][col].addDropHandler(new DropHandler() {
						@Override
						public void onDrop(DropEvent event) {
							event.preventDefault();
							presenter.selectPiece(i, j);
						}
					});
				}

				boardContainer[row][col] = new AbsolutePanel();
				boardContainer[row][col].setHeight("45px");
				boardContainer[row][col].setWidth("45px");
				boardContainer[row][col].add(board[row][col], 0, 0);
				gameGrid.setWidget(row, col, boardContainer[row][col]);
			}
		}

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				isReleased[row][col] = false;
			}
		}

		if (MediaElement.CAN_PLAY_PROBABLY.equals(moveAudio
				.canPlayType(AudioElement.TYPE_OGG))) {
			moveAudio.setSrc("kanwang_audio/move.ogg");
			overAudio.setSrc("kanwang_audio/over.ogg");
		} else if (MediaElement.CAN_PLAY_PROBABLY.equals(moveAudio
				.canPlayType(AudioElement.TYPE_MP3))) {
			moveAudio.setSrc("kanwang_audio/move.mp3");
			overAudio.setSrc("kanwang_audio/over.mp3");
		} else if (MediaElement.CAN_PLAY_MAYBE.equals(moveAudio
				.canPlayType(AudioElement.TYPE_OGG))) {
			moveAudio.setSrc("kanwang_audio/move.ogg");
			overAudio.setSrc("kanwang_audio/over.ogg");
		} else if (MediaElement.CAN_PLAY_MAYBE.equals(moveAudio
				.canPlayType(AudioElement.TYPE_MP3))) {
			moveAudio.setSrc("kanwang_audio/move.mp3");
			overAudio.setSrc("kanwang_audio/over.mp3");
		}

	}

	public void init() {
		presenter.init_state();
	}

	private void init_matchPanel() {
		final TextBox t = new TextBox();
		t.getElement().getStyle().setFloat(Style.Float.LEFT);
		t.getElement().getStyle().setPosition(Position.RELATIVE);

		Button create = new Button();
		create.setText(constants.buttonNewMatch());
		create.getElement().getStyle().setFloat(Style.Float.LEFT);
		create.getElement().getStyle().setPosition(Position.RELATIVE);
		create.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GameServiceAsync game = GWT.create(GameService.class);
				game.createMatch(user.getEmailAddress(), t.getText(),
						new AsyncCallback<Match>() {
							public void onFailure(Throwable error) {
							}

							@Override
							public void onSuccess(Match result) {
								if (result == null)
									Window.alert(constants
											.alertWrongMatchInfo());
								if (result != null) {
									presenter.setMatch(result);
									Window.alert(messages
											.alertCreateMatch(result.getID()
													+ ""));
									presenter.setMatchInfo();
									int n = matchInfo.getItemCount();
									for (int i = 0; i < n; i++) {
										if (matchInfo
												.getItemText(i)
												.startsWith(result.getID() + "")) {
											matchInfo.setSelectedIndex(i);
											break;
										}
									}
									GameServiceAsync game = GWT
											.create(GameService.class);
									game.getMatchWithID("" + result.getID(),
											new AsyncCallback<Match>() {
												public void onFailure(
														Throwable error) {
												}

												@Override
												public void onSuccess(
														Match result) {
													if (result == null)
														Window.alert(constants
																.alertWrongMatchInfo());
													try {
														presenter.state = Presenter
																.getStateFromString(result
																		.getState());
														presenter
																.setMatch(result);
														init();
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											});
								}
							}
						});
			}
		});

		Button autoMatch = new Button();
		autoMatch.setText(constants.buttonAutoMatch());
		autoMatch.getElement().getStyle().setFloat(Style.Float.LEFT);
		autoMatch.getElement().getStyle().setPosition(Position.RELATIVE);
		autoMatch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GameServiceAsync game = GWT.create(GameService.class);
				game.autoCreate(user.getEmailAddress(),
						new AsyncCallback<Match>() {
							public void onFailure(Throwable error) {
							}

							@Override
							public void onSuccess(Match result) {
								if (result == null)
									Window.alert(constants.alertAutoMatchWait());
								if (result != null) {
									presenter.setMatch(result);
									Window.alert(messages
											.alertCreateMatch(result.getID()
													+ ""));
									presenter.setMatchInfo();
									int n = matchInfo.getItemCount();
									for (int i = 0; i < n; i++) {
										if (matchInfo
												.getItemText(i)
												.startsWith(result.getID() + "")) {
											matchInfo.setSelectedIndex(i);
											break;
										}
									}
									GameServiceAsync game = GWT
											.create(GameService.class);
									game.getMatchWithID("" + result.getID(),
											new AsyncCallback<Match>() {
												public void onFailure(
														Throwable error) {
												}

												@Override
												public void onSuccess(
														Match result) {
													if (result == null)
														Window.alert(constants
																.alertWrongMatchInfo());
													try {
														presenter.state = Presenter
																.getStateFromString(result
																		.getState());
														presenter
																.setMatch(result);
														init();
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											});
								}
							}
						});
			}
		});

		Button delete = new Button();
		delete.setText(constants.buttonDeleteMatch());
		delete.getElement().getStyle().setFloat(Style.Float.RIGHT);
		delete.getElement().getStyle().setPosition(Position.RELATIVE);
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GameServiceAsync game = GWT.create(GameService.class);
				String s = matchInfo.getItemText(matchInfo.getSelectedIndex());
				String id;
				if (s.startsWith("游戏")) {
					id = s.substring(2, s.indexOf("："));
				} else {
					String temp[] = s.split(" ");
					id = temp[0].substring(0, temp[0].length() - 1);
				}
				long mid = Long.parseLong(id);
				game.deleteMatch(mid, user.getEmailAddress(),
						new AsyncCallback<Match>() {
							public void onFailure(Throwable error) {
							}

							@Override
							public void onSuccess(Match result) {
								if (result != null) {
									presenter.setMatch(result);
									Window.alert(messages
											.alertDeleteMatch(result.getID()
													+ ""));
									presenter.setMatchInfo();
								}
							}
						});
			}
		});
		
		Button playAI = new Button();
		playAI.setText(constants.buttonAI());
		playAI.getElement().getStyle().setFloat(Style.Float.RIGHT);
		playAI.getElement().getStyle().setPosition(Position.RELATIVE);
		playAI.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Match m =new Match(new State());
				m.setW(user.getEmailAddress());
				m.setB("AI");
				m.setIDAI();
				presenter.setMatch(m);
				presenter.init_state();
				presenter.setState(new State());
				presenter.setMatchInfo();
				presenter.setPlayAI(true);
			}
		});

		matchPanel.add(t);
		matchPanel.add(create);
		matchPanel.add(autoMatch);
		matchPanel.add(delete);
		matchPanel.add(playAI);
	}

	private void init_promotion() {
		promotionSelect.resize(1, 4);
		promotionSelect.setCellPadding(0);
		promotionSelect.setCellSpacing(0);
		promotionSelect.setBorderWidth(2);
		promotionSelect.getElement().getStyle().setPosition(Position.ABSOLUTE);
		promotionSelect.getElement().getStyle()
				.setTop(gridTop + 45 * 8 + 30, Unit.PX);
		promotionSelect.getElement().getStyle()
				.setLeft(gridLeft + 45 * 2 - 8, Unit.PX);

		for (int i = 0; i < 4; i++) {
			final Image pimage = new Image();
			final int pCol = i;
			promotion[i] = pimage;
			switch (i) {
			case 0:
				pimage.setResource(gameImages.whiteBishop());
				break;
			case 1:
				pimage.setResource(gameImages.whiteQueen());
				break;
			case 2:
				pimage.setResource(gameImages.whiteKnight());
				break;
			case 3:
				pimage.setResource(gameImages.whiteRook());
				break;
			default:
				break;
			}
			pimage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.handlePromotion(0, pCol);
				}
			});
			promotionSelect.setWidget(0, pCol, promotion[i]);

		}
	}

	@Override
	public void setPiece(int row, int col, Piece piece) {
		row = 7 - row;
		if (boardContainer[row][col].getWidgetCount() == 2)
			boardContainer[row][col].remove(1);
		final Image temp = new Image();
		pieceImage[row][col] = temp;
		temp.setWidth("100%");
		if (piece == null) {
			return;
		}
		if (piece.getColor() == Color.WHITE) {
			switch (piece.getKind()) {
			case BISHOP:
				temp.setResource(gameImages.whiteBishop());
				break;
			case KING:
				temp.setResource(gameImages.whiteKing());
				break;
			case KNIGHT:
				temp.setResource(gameImages.whiteKnight());
				break;
			case PAWN:
				temp.setResource(gameImages.whitePawn());
				break;
			case QUEEN:
				temp.setResource(gameImages.whiteQueen());
				break;
			case ROOK:
				temp.setResource(gameImages.whiteRook());
				break;
			default:
				break;
			}
		} else {
			switch (piece.getKind()) {
			case BISHOP:
				temp.setResource(gameImages.blackBishop());
				break;
			case KING:
				temp.setResource(gameImages.blackKing());
				break;
			case KNIGHT:
				temp.setResource(gameImages.blackKnight());
				break;
			case PAWN:
				temp.setResource(gameImages.blackPawn());
				break;
			case QUEEN:
				temp.setResource(gameImages.blackQueen());
				break;
			case ROOK:
				temp.setResource(gameImages.blackRook());
				break;
			default:
				break;
			}
		}

		final int i = row;
		final int j = col;
		temp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectPiece(i, j);
			}
		});
		temp.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				presenter.overCell(i, j);
			}
		});

		temp.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				presenter.outCell(i, j);
			}
		});
		// drag and drop
		if (this.useDragDrop) {
			temp.getElement().setDraggable(Element.DRAGGABLE_TRUE);
			temp.addDragStartHandler(new DragStartHandler() {
				@Override
				public void onDragStart(DragStartEvent event) {
					event.setData("text", i + " " + j);
					presenter.selectPiece(i, j);
				}
			});
			temp.addDragOverHandler(new DragOverHandler() {
				@Override
				public void onDragOver(DragOverEvent event) {
					presenter.overCell(i, j);
				}
			});
			temp.addDragLeaveHandler(new DragLeaveHandler() {
				@Override
				public void onDragLeave(DragLeaveEvent event) {
					presenter.outCell(i, j);
				}
			});
			temp.addDropHandler(new DropHandler() {
				@Override
				public void onDrop(DropEvent event) {
					event.preventDefault();
					presenter.selectPiece(i, j);
				}
			});
		}

		temp.getElement().getStyle().setZIndex(4000);
		boardContainer[row][col].add(temp, 0, 0);

	}

	/**
	 * @param row
	 * @param col
	 * @param release
	 */

	private void releaseContainer(int row, int col, boolean release) {
		Element element = board[row][col].getElement();
		if (release) {
			element.getStyle().setWidth(41, Unit.PX);
			element.getStyle().setHeight(41, Unit.PX);
			boardContainer[row][col].setWidgetPosition(
					boardContainer[row][col].getWidget(0), 2, 2);
			if (boardContainer[row][col].getWidgetCount() == 2)
				boardContainer[row][col].setWidgetPosition(
						boardContainer[row][col].getWidget(1), 0, 0);
		} else {
			element.getStyle().setWidth(45, Unit.PX);
			element.getStyle().setHeight(45, Unit.PX);
			boardContainer[row][col].setWidgetPosition(
					boardContainer[row][col].getWidget(0), 0, 0);
			if (boardContainer[row][col].getWidgetCount() == 2)
				boardContainer[row][col].setWidgetPosition(
						boardContainer[row][col].getWidget(1), 0, 0);
		}
	}

	@Override
	public void setHighlighted(int row, int col, boolean highlighted) {
		Element element = board[row][col].getElement();
		if (!isReleased[row][col])
			releaseContainer(row, col, highlighted);
		if (highlighted) {
			element.addClassName(css.highlighted());
		} else {
			element.removeClassName(css.highlighted());
		}
	}

	@Override
	public void setWhoseTurn(Color color) {
		gameStatus.setText((color == Color.WHITE ? constants.labelWhiteTurn()
				: constants.labelBlackTurn()));
	}

	@Override
	public void setGameResult(GameResult gameResult) {
		if (gameResult == null)
			return;
		this.playOverSound();
		String text = "";
		switch (gameResult.getGameResultReason()) {
		case CHECKMATE:
			text = text
					+ (gameResult.getWinner() == Color.WHITE ? constants
							.labelGameOverWhite() : constants
							.labelGameOverBlack());
			break;
		case FIFTY_MOVE_RULE:
			text = text + constants.labelGameOverfifty();
			break;
		case STALEMATE:
			text = text + constants.labelGameOverstale();
			break;
		case THREEFOLD_REPETITION_RULE:
			break;
		default:
			break;
		}
		gameStatus.setText(text);
	}

	@Override
	public void setSelected(int row, int col, boolean selected) {
		Element element = board[row][col].getElement();
		releaseContainer(row, col, selected);
		isReleased[row][col] = selected;
		if (selected) {
			element.setClassName(css.selected());
		} else {
			element.removeClassName(css.selected());
		}
	}

	@Override
	public void clearBoard() {
		Image image;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				image = board[row][col];
				if (row % 2 == 0 && col % 2 == 1 || row % 2 == 1
						&& col % 2 == 0) {
					image.setResource(gameImages.blackTile());
				} else {
					image.setResource(gameImages.whiteTile());
				}
			}
		}
	}

	@Override
	public void showEndOnBoard(int row, int col, boolean isEnd) {
		Element element = board[row][col].getElement();
		releaseContainer(row, col, isEnd);
		isReleased[row][col] = isEnd;
		if (isEnd) {
			element.setClassName(css.showEnd());
		} else {
			element.removeClassName(css.showEnd());
		}
	}

	@Override
	public void showPromotionOnBoard(boolean isWaiting) {
		promotionSelect.setVisible(isWaiting);
	}

	@Override
	public void setAnimationBegin(int row, int col) {
		boardContainer[row][col].remove(1);
		moveContainer.setVisible(true);
		moveContainer.setWidget(pieceImage[row][col]);
		pieceImage[row][col].getElement().getStyle().setZIndex(5000);
		removeHandler = pieceImage[row][col]
				.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {

					}
				});
		removeHandler = pieceImage[row][col]
				.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						removeHandler.removeHandler();
					}
				});

		removeHandler = pieceImage[row][col]
				.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						removeHandler.removeHandler();
					}
				});

		moveContainer.getElement().getStyle().setZIndex(5000);
		moveContainer.getElement().getStyle().setWidth(45, Unit.PX);
		moveContainer.getElement().getStyle().setHeight(45, Unit.PX);
		moveContainer.getElement().getStyle()
				.setTop(gridTop + row * 45, Unit.PX);
		moveContainer.getElement().getStyle()
				.setLeft(gridLeft + col * 45, Unit.PX);
		moveContainer.getElement().getStyle().setPosition(Position.ABSOLUTE);
	}

	@Override
	public void setAnimationEnd(int row, int col) {
		moveContainer.getElement().getStyle()
				.setTop(gridTop + row * 45, Unit.PX);
		moveContainer.getElement().getStyle()
				.setLeft(gridLeft + col * 45, Unit.PX);
		moveContainer.getElement().getStyle().setPosition(Position.ABSOLUTE);
		moveContainer.clear();
		moveContainer.setVisible(false);
		presenter.setState(presenter.state);
		this.playMoveSound();
	}

	@Override
	public void setAnimaitonUpdate(int frow, int fcol, int trow, int tcol,
			double progress) {
		double row, col;
		row = gridTop + frow * 45 + (trow - frow) * 45 * progress;
		col = gridLeft + fcol * 45 + (tcol - fcol) * 45 * progress;
		moveContainer.getElement().getStyle().setTop(row, Unit.PX);
		moveContainer.getElement().getStyle().setLeft(col, Unit.PX);
		moveContainer.getElement().getStyle().setPosition(Position.ABSOLUTE);
	}

	@Override
	public boolean useAnimation() {
		return this.useAnimation;
	}

	@Override
	public void playMoveSound() {
		moveAudio.play();
	}

	@Override
	public void playOverSound() {
		overAudio.play();
	}

	// @Override
	// public void setHistory() {
	// String s = Presenter.makeStringfromState(presenter.state);
	// History.newItem(s);
	// }

	// @Override
	// public void saveLocalMemory() {
	// store.setItem("state", Presenter.makeStringfromState(presenter.state));
	// }
	//
	// @Override
	// public State loadLocalMemory() {
	// try {
	// return Presenter.getStateFromString(store.getItem("state"));
	// } catch (Exception e) {
	// return new State();
	// }
	// }

	@Override
	public void setMatchInfo() {
		presenter.setPlayAI(false);
		GameServiceAsync game = GWT.create(GameService.class);
		matchInfo.clear();
		matchInfo.addItem(constants.newgame());
		game.getMatches(user.getEmailAddress(),
				new AsyncCallback<LinkedList<Match>>() {
					public void onFailure(Throwable error) {
					}

					@Override
					public void onSuccess(LinkedList<Match> result) {
						for (Match m : result) {
							if (m.isDeleted(user.getEmailAddress()))
								continue;
							if (presenter.getMatch() != null
									&& m.getID() == presenter.getMatch()
											.getID()) {
								matchDate.setText(messages.labelStartDate(m
										.getStartDate()));
							}
							String temp = "";
							try {
								State s = Presenter.getStateFromString(m
										.getState());
								if (s.getGameResult() == null) {
									if (m.getW().equals(user.getEmailAddress())) {
										temp += messages.boxInGameStatusWhite(
												m.getID() + "",
												m.getAnotherUser(user
														.getEmailAddress()));
									} else {
										temp += messages.boxInGameStatusBlack(
												m.getID() + "",
												m.getAnotherUser(user
														.getEmailAddress()));
									}
									if (m.getTurn() == Color.WHITE)
										temp += messages.boxWhosTurn(m.getW());
									else
										temp += messages.boxWhosTurn(m.getB());
								} else {
									String text = "";
									switch (s.getGameResult()
											.getGameResultReason()) {
									case CHECKMATE:
										if (s.getGameResult().getWinner() == Color.WHITE)
											text = text
													+ messages.boxGameOverWin(
															m.getID() + "",
															m.getW());
										else
											text = text
													+ messages.boxGameOverWin(
															m.getID() + "",
															m.getB());
										break;
									case FIFTY_MOVE_RULE:
										text = text
												+ messages.boxGameOverfifty(m
														.getID() + "");
										break;
									case STALEMATE:
										text = text
												+ messages.boxGameOverstale(m
														.getID() + "");
										break;
									case THREEFOLD_REPETITION_RULE:
										break;
									default:
										break;
									}
									temp += text;
								}

							} catch (Exception e) {
								e.printStackTrace();
							}

							matchInfo.addItem(temp);
						}
						int n = matchInfo.getItemCount();
						if(presenter.getPlayAI()){
							matchInfo.setSelectedIndex(0);
							return;
						}
						if (presenter.getMatch() == null)
							return;
						for (int i = 0; i < n; i++) {
							String temp = matchInfo.getItemText(i);
							if (temp.startsWith("游戏"))
								temp = temp.substring(2);
							if (temp.startsWith(""
									+ presenter.getMatch().getID())) {
								matchInfo.setSelectedIndex(i);
								return;
							}
						}
					}
				});
	}

	@Override
	public void updateRating() {
		GameServiceAsync game = GWT.create(GameService.class);
		game.updateRating(presenter.getMatch().getID()+"",
				new AsyncCallback<Player>() {
					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Player result) {
					}
				});
	}

	@Override
	public void setRatingInfo(int rating) {
		ratingInfo.setText(messages.labelRating(rating+""));
	}

}
