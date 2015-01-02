package org.jetbrains.rpc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.jsonProtocol.Request;

public abstract class CommandSenderBase<SUCCESS_RESPONSE, ERROR_DETAILS> implements CommandSender {
  protected abstract <RESULT> void send(@NotNull Request message, @NotNull RequestPromise<SUCCESS_RESPONSE, RESULT, ERROR_DETAILS> callback);

  @Override
  @NotNull
  public final <RESULT> Promise<RESULT> send(@NotNull Request<RESULT> request) {
    RequestPromise<SUCCESS_RESPONSE, RESULT, ERROR_DETAILS> callback = new RequestPromise<SUCCESS_RESPONSE, RESULT, ERROR_DETAILS>(request.getMethodName());
    send(request, callback);
    return callback;
  }

  protected static final class RequestPromise<SUCCESS_RESPONSE, RESULT, ERROR_DETAILS> extends AsyncPromise<RESULT> implements RequestCallback<SUCCESS_RESPONSE, ERROR_DETAILS> {
    private final String methodName;

    public RequestPromise(@Nullable String methodName) {
      this.methodName = methodName;
    }

    @Override
    public void onSuccess(SUCCESS_RESPONSE response, @Nullable ResultReader<SUCCESS_RESPONSE> resultReader) {
      try {
        if (resultReader == null) {
          //noinspection unchecked
          setResult((RESULT)response);
        }
        else {
          setResult(methodName == null ? null : resultReader.<RESULT>readResult(methodName, response));
        }
      }
      catch (Throwable e) {
        CommandProcessor.LOG.error(e);
        setError(e.getMessage());
      }
    }

    @Override
    public void onError(@NotNull String errorMessage, ERROR_DETAILS details) {
      setError(errorMessage);
    }
  }
}