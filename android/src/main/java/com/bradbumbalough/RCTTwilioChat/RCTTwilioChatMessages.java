package com.bradbumbalough.RCTTwilioChat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import com.twilio.chat.Messages;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Message;
import com.twilio.chat.Channel;
import com.twilio.chat.StatusListener;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.Member;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.json.JSONObject;

public class RCTTwilioChatMessages extends ReactContextBaseJavaModule {

    @Override
    public String getName() {
        return "TwilioChatMessages";
    }

    public RCTTwilioChatMessages(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void loadMessagesFromChannelSid(String sid, final CallbackListener<Messages> callbackListener) {
        RCTTwilioChatClient.getInstance().client.getChannels().getChannel(sid, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(final Channel channel) {
                if ( channel.getSynchronizationStatus() == Channel.SynchronizationStatus.ALL ) {
                    callbackListener.onSuccess(channel.getMessages());
                } else {
                    ChannelListener listener = new ChannelListener() {

                        @Override
                        public void onMessageAdded(Message member) {}

                        @Override
                        public void onMessageUpdated(Message message, Message.UpdateReason reason) {}

                        @Override
                        public void onMessageDeleted(Message message) {}

                        @Override
                        public void onMemberAdded(Member member) {}

                        @Override
                        public void onMemberUpdated(Member member, Member.UpdateReason reason) {}

                        @Override
                        public void onMemberDeleted(Member member) {}

                        @Override
                        public void onTypingStarted(Channel channel, Member member) {

                        }

                        @Override
                        public void onTypingEnded(Channel channel, Member member) {

                        }

                        @Override
                        public void onSynchronizationChanged(Channel channel) {
                            if ( channel.getSynchronizationStatus() == Channel.SynchronizationStatus.ALL ) {
                                callbackListener.onSuccess(channel.getMessages());
                            }
                        }
                    };
                }
            }

            @Override
            public void onError(final ErrorInfo errorInfo) {
                callbackListener.onError(errorInfo);
            }
        });
    }

    @ReactMethod
    public void getLastConsumedMessageIndex(String channelSid, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("get-last-consumped-message-index","Error occurred while attempting to getLastConsumedMessageIndex.");
            }

            @Override
            public void onSuccess(Messages messages) {
                Long lastConsumedMessageIndex = messages.getLastConsumedMessageIndex();
                if (lastConsumedMessageIndex != null) {
                    promise.resolve(Integer.valueOf(lastConsumedMessageIndex.intValue()));
                } else {
                    promise.resolve(null);
                }
            }
        });
    }

    @ReactMethod
    public void sendMessage(String channelSid, final String body, final ReadableMap attributes, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("send-message-error","Error occurred while attempting to sendMessage.");
            }

            @Override
            public void onSuccess(final Messages messages) {
                final Message.Options options = Message.options().withBody(body);

                final CallbackListener<Message> sendListener = new CallbackListener<Message>() {
                  @Override
                  public void onError(ErrorInfo errorInfo) {
                      super.onError(errorInfo);
                      promise.reject("send-message-error","Error occurred while attempting to sendMessage.");
                  }

                  @Override
                  public void onSuccess(Message message) {
                      promise.resolve(true);
                  }
                };

                if(attributes != null) {
                  final JSONObject json = RCTConvert.readableMapToJson(attributes);
                    options.withAttributes(json);
                    messages.sendMessage(options, sendListener);
                } else {
                  messages.sendMessage(options, sendListener);
                }
            }
        });
    }

    @ReactMethod
    public void removeMessage(String channelSid, final Integer index, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("remove-message-error","Error occurred while attempting to remove message.");
            }

            @Override
            public void onSuccess(final Messages messages) {
                messages.getMessageByIndex(index, new CallbackListener<Message>() {
                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        super.onError(errorInfo);
                        promise.reject("remove-message-error","Error occurred while attempting to remove message.");
                    }

                    @Override
                    public void onSuccess(Message message) {
                        messages.removeMessage(message, new StatusListener() {
                            @Override
                            public void onError(ErrorInfo errorInfo) {
                                super.onError(errorInfo);
                                promise.reject("remove-message-error","Error occurred while attempting to remove message.");
                            }

                            @Override
                            public void onSuccess() {
                                promise.resolve(true);
                            }
                        });
                    }
                });
            }
        });
    }

    @ReactMethod
    public void getLastMessages(String channelSid, final Integer count, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("get-last-messages-error","Error occurred while attempting to getLastMessages.");
            }

            public void onSuccess(Messages messages) {
                if ( messages == null ) {
                    List<Message> _messages = new ArrayList<Message>();
                    promise.resolve(RCTConvert.Messages(_messages));
                }
                else {
                    messages.getLastMessages(count, new CallbackListener<List<Message>>() {
                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            super.onError(errorInfo);
                            promise.reject("get-last-messages-error", "Error occurred while attempting to getLastMessages.");
                        }

                        @Override
                        public void onSuccess(List<Message> _messages) {
                            promise.resolve(RCTConvert.Messages(_messages));
                        }
                    });
                }
            }
        });
    }

    @ReactMethod
    public void getMessagesAfter(String channelSid, final Integer index, final Integer count, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("get-messages-after-error","Error occurred while attempting to getMessagesAfter.");
            }

            public void onSuccess(Messages messages) {
                messages.getMessagesAfter(index, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        super.onError(errorInfo);
                        promise.reject("get-messages-after-error","Error occurred while attempting to getMessagesAfter.");
                    }

                    @Override
                    public void onSuccess(List<Message> _messages) {
                        promise.resolve(RCTConvert.Messages(_messages));
                    }
                });
            }
        });
    }

    @ReactMethod
    public void getMessagesBefore(String channelSid, final Integer index, final Integer count, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("get-messages-before-error","Error occurred while attempting to getMessagesBefore.");
            }

            public void onSuccess(Messages messages) {
                messages.getMessagesBefore(index, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        super.onError(errorInfo);
                        promise.reject("get-messages-before-error","Error occurred while attempting to getMessagesBefore.");
                    }

                    @Override
                    public void onSuccess(List<Message> _messages) {
                        promise.resolve(RCTConvert.Messages(_messages));
                    }
                });
            }
        });
    }

    @ReactMethod
    public void getMessage(String channelSid, final Integer index, final Promise promise) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("get-message-error","Error occurred while attempting to getMessage.");
            }

            public void onSuccess(Messages messages) {
                messages.getMessageByIndex(index, new CallbackListener<Message>() {
                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        super.onError(errorInfo);
                        promise.reject("get-message-error","Error occurred while attempting to getMessage.");
                    }

                    @Override
                    public void onSuccess(Message message) {
                        promise.resolve(RCTConvert.Message(message));
                    }
                });
            }
        });
    }

    @ReactMethod
    public void setLastConsumedMessageIndex(String channelSid, final Integer index) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onSuccess(Messages messages) {
                messages.advanceLastConsumedMessageIndexWithResult(index, new CallbackListener<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {

                    }
                });

            }
        });
    }

    @ReactMethod
    public void advanceLastConsumedMessageIndex(String channelSid, final Integer index) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onSuccess(Messages messages) {
                messages.advanceLastConsumedMessageIndexWithResult(index, new CallbackListener<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {

                    }
                });
            }
        });
    }

    @ReactMethod
    public void setAllMessagesConsumed(String channelSid) {
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onSuccess(Messages messages) {
                messages.setAllMessagesConsumedWithResult(new CallbackListener<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {

                    }
                });
            }
        });
    }

    // Message instance method

    @ReactMethod
    public void updateBody(String channelSid, final Integer index, final String body, final Promise promise) {;
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("update-body-error","Error occurred while attempting to updateBody.");
            }

            public void onSuccess(Messages messages) {
                messages.getMessageByIndex(index, new CallbackListener<Message>() {
                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        super.onError(errorInfo);
                        promise.reject("update-body-error","Error occurred while attempting to updateBody.");
                    }

                    @Override
                    public void onSuccess(Message message) {
                        message.updateMessageBody(body, new StatusListener() {
                            @Override
                            public void onError(ErrorInfo errorInfo) {
                                super.onError(errorInfo);
                                promise.reject("update-body-error","Error occurred while attempting to updateBody.");
                            }

                            @Override
                            public void onSuccess() {
                                promise.resolve(true);
                            }
                        });
                    }
                });
            }
        });
    }

    @ReactMethod
    public void setAttributes(String channelSid, final Integer index, ReadableMap attributes, final Promise promise) {
        final JSONObject json = RCTConvert.readableMapToJson(attributes);
        loadMessagesFromChannelSid(channelSid, new CallbackListener<Messages>() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
                promise.reject("set-attributes-error", "Error occurred while attempting to setAttributes on Message.");
            }

            public void onSuccess(Messages messages) {
                messages.getMessageByIndex(index, new CallbackListener<Message>() {
                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        super.onError(errorInfo);
                        promise.reject("set-attributes-error", "Error occurred while attempting to setAttributes on Message.");
                    }

                    @Override
                    public void onSuccess(Message message) {
                        message.setAttributes(json, new StatusListener() {
                            @Override
                            public void onError(ErrorInfo errorInfo) {
                                super.onError(errorInfo);
                                promise.reject("set-attributes-error", "Error occurred while attempting to setAttributes on Message.");
                            }

                            @Override
                            public void onSuccess() {
                                promise.resolve(true);
                            }
                        });
                    }
                });
            }
        });
    }

}
