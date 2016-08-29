package org.lambdamatic.internal.elasticsearch.reactivestreams;

import java.util.function.Consumer;

import org.lambdamatic.internal.elasticsearch.clientdsl.responses.IndexDocumentResponse;
import org.lambdamatic.internal.elasticsearch.codec.CodecRegistry;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A {@link Subscriber} for the index document operation.
 * 
 * @param <D>
 */
public class IndexDocumentResponseSubscriber<D> implements Subscriber<IndexDocumentResponse> {

  private final D document;

  private final Consumer<D> onSuccessHandler;

  private final Consumer<Throwable> onErrorHandler;

  private final CodecRegistry codecRegistry;

  public IndexDocumentResponseSubscriber(final D document, final CodecRegistry codecRegistry,
      final Consumer<D> onSuccessHandler, final Consumer<Throwable> onErrorHandler) {
    this.document = document;
    this.codecRegistry = codecRegistry;
    this.onSuccessHandler = onSuccessHandler;
    this.onErrorHandler = onErrorHandler;
  }

  @Override
  public void onSubscribe(final Subscription s) {
    s.request(1);
  }

  @Override
  public void onNext(final IndexDocumentResponse indexDocumentResponse) {
    final DocumentCodec<D> documentCodec = this.codecRegistry.getDocumentCodec(document);
    final String documentId = documentCodec.getDomainObjectId(document);
    if (documentId == null) {
      documentCodec.setDomainObjectId(document, indexDocumentResponse.getId());
    }
    onSuccessHandler.accept(document);
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
