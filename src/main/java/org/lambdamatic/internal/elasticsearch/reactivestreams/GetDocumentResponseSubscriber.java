package org.lambdamatic.internal.elasticsearch.reactivestreams;

import java.util.function.Consumer;

import org.lambdamatic.elasticsearch.exceptions.DocumentNotFoundException;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetDocumentResponse;
import org.lambdamatic.internal.elasticsearch.codec.CodecRegistry;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A {@link Subscriber} for the index document operation.
 * 
 * @param <D>
 */
public class GetDocumentResponseSubscriber<D> implements Subscriber<GetDocumentResponse> {

  private final Consumer<D> onSuccessHandler;

  private final Consumer<Throwable> onErrorHandler;

  private final CodecRegistry codecRegistry;

  private String indexName;

  private String type;

  private String documentId;

  private Class<D> domainType;

  public GetDocumentResponseSubscriber(final CodecRegistry codecRegistry, final String indexName,
      final String type, final String documentId, final Class<D> domainType, 
      final Consumer<D> onSuccessHandler, final Consumer<Throwable> onErrorHandler) {
    this.codecRegistry = codecRegistry;
    this.indexName = indexName;
    this.type = type;
    this.documentId = documentId;
    this.domainType = domainType;
    this.onSuccessHandler = onSuccessHandler;
    this.onErrorHandler = onErrorHandler;
  }

  @Override
  public void onSubscribe(final Subscription s) {
    s.request(1);
  }

  @Override
  public void onNext(final GetDocumentResponse getDocumentResponse) {
    if (getDocumentResponse.isExists()) {
      final D document = this.codecRegistry.getDocumentCodec(getDocumentResponse.getSource(), this.domainType)
          .decode(documentId, getDocumentResponse.getSource());
      onSuccessHandler.accept(document);
    } else {
      onErrorHandler.accept(new DocumentNotFoundException(this.indexName, this.type, documentId));
    }
  }

  @Override
  public void onError(Throwable t) {
    onErrorHandler.accept(t);
  }

  @Override
  public void onComplete() {
    // do nothing, we are only collecting a single element in this case.
  }
}
