package fr.insa.mesh.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.65.1)",
    comments = "Source: labotrack.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SampleServiceGrpc {

  private SampleServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "labotrack.SampleService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<fr.insa.mesh.grpc.GetSampleRequest,
      fr.insa.mesh.grpc.SampleResponse> getGetSampleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSample",
      requestType = fr.insa.mesh.grpc.GetSampleRequest.class,
      responseType = fr.insa.mesh.grpc.SampleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<fr.insa.mesh.grpc.GetSampleRequest,
      fr.insa.mesh.grpc.SampleResponse> getGetSampleMethod() {
    io.grpc.MethodDescriptor<fr.insa.mesh.grpc.GetSampleRequest, fr.insa.mesh.grpc.SampleResponse> getGetSampleMethod;
    if ((getGetSampleMethod = SampleServiceGrpc.getGetSampleMethod) == null) {
      synchronized (SampleServiceGrpc.class) {
        if ((getGetSampleMethod = SampleServiceGrpc.getGetSampleMethod) == null) {
          SampleServiceGrpc.getGetSampleMethod = getGetSampleMethod =
              io.grpc.MethodDescriptor.<fr.insa.mesh.grpc.GetSampleRequest, fr.insa.mesh.grpc.SampleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSample"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  fr.insa.mesh.grpc.GetSampleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  fr.insa.mesh.grpc.SampleResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SampleServiceMethodDescriptorSupplier("GetSample"))
              .build();
        }
      }
    }
    return getGetSampleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<fr.insa.mesh.grpc.UpdateStatusRequest,
      fr.insa.mesh.grpc.UpdateStatusResponse> getUpdateStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateStatus",
      requestType = fr.insa.mesh.grpc.UpdateStatusRequest.class,
      responseType = fr.insa.mesh.grpc.UpdateStatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<fr.insa.mesh.grpc.UpdateStatusRequest,
      fr.insa.mesh.grpc.UpdateStatusResponse> getUpdateStatusMethod() {
    io.grpc.MethodDescriptor<fr.insa.mesh.grpc.UpdateStatusRequest, fr.insa.mesh.grpc.UpdateStatusResponse> getUpdateStatusMethod;
    if ((getUpdateStatusMethod = SampleServiceGrpc.getUpdateStatusMethod) == null) {
      synchronized (SampleServiceGrpc.class) {
        if ((getUpdateStatusMethod = SampleServiceGrpc.getUpdateStatusMethod) == null) {
          SampleServiceGrpc.getUpdateStatusMethod = getUpdateStatusMethod =
              io.grpc.MethodDescriptor.<fr.insa.mesh.grpc.UpdateStatusRequest, fr.insa.mesh.grpc.UpdateStatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  fr.insa.mesh.grpc.UpdateStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  fr.insa.mesh.grpc.UpdateStatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SampleServiceMethodDescriptorSupplier("UpdateStatus"))
              .build();
        }
      }
    }
    return getUpdateStatusMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SampleServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SampleServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SampleServiceStub>() {
        @java.lang.Override
        public SampleServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SampleServiceStub(channel, callOptions);
        }
      };
    return SampleServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SampleServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SampleServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SampleServiceBlockingStub>() {
        @java.lang.Override
        public SampleServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SampleServiceBlockingStub(channel, callOptions);
        }
      };
    return SampleServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SampleServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SampleServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SampleServiceFutureStub>() {
        @java.lang.Override
        public SampleServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SampleServiceFutureStub(channel, callOptions);
        }
      };
    return SampleServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getSample(fr.insa.mesh.grpc.GetSampleRequest request,
        io.grpc.stub.StreamObserver<fr.insa.mesh.grpc.SampleResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetSampleMethod(), responseObserver);
    }

    /**
     */
    default void updateStatus(fr.insa.mesh.grpc.UpdateStatusRequest request,
        io.grpc.stub.StreamObserver<fr.insa.mesh.grpc.UpdateStatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateStatusMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service SampleService.
   */
  public static abstract class SampleServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return SampleServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service SampleService.
   */
  public static final class SampleServiceStub
      extends io.grpc.stub.AbstractAsyncStub<SampleServiceStub> {
    private SampleServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SampleServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SampleServiceStub(channel, callOptions);
    }

    /**
     */
    public void getSample(fr.insa.mesh.grpc.GetSampleRequest request,
        io.grpc.stub.StreamObserver<fr.insa.mesh.grpc.SampleResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetSampleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateStatus(fr.insa.mesh.grpc.UpdateStatusRequest request,
        io.grpc.stub.StreamObserver<fr.insa.mesh.grpc.UpdateStatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateStatusMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service SampleService.
   */
  public static final class SampleServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<SampleServiceBlockingStub> {
    private SampleServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SampleServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SampleServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public fr.insa.mesh.grpc.SampleResponse getSample(fr.insa.mesh.grpc.GetSampleRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetSampleMethod(), getCallOptions(), request);
    }

    /**
     */
    public fr.insa.mesh.grpc.UpdateStatusResponse updateStatus(fr.insa.mesh.grpc.UpdateStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service SampleService.
   */
  public static final class SampleServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<SampleServiceFutureStub> {
    private SampleServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SampleServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SampleServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<fr.insa.mesh.grpc.SampleResponse> getSample(
        fr.insa.mesh.grpc.GetSampleRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetSampleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<fr.insa.mesh.grpc.UpdateStatusResponse> updateStatus(
        fr.insa.mesh.grpc.UpdateStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateStatusMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_SAMPLE = 0;
  private static final int METHODID_UPDATE_STATUS = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_SAMPLE:
          serviceImpl.getSample((fr.insa.mesh.grpc.GetSampleRequest) request,
              (io.grpc.stub.StreamObserver<fr.insa.mesh.grpc.SampleResponse>) responseObserver);
          break;
        case METHODID_UPDATE_STATUS:
          serviceImpl.updateStatus((fr.insa.mesh.grpc.UpdateStatusRequest) request,
              (io.grpc.stub.StreamObserver<fr.insa.mesh.grpc.UpdateStatusResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetSampleMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              fr.insa.mesh.grpc.GetSampleRequest,
              fr.insa.mesh.grpc.SampleResponse>(
                service, METHODID_GET_SAMPLE)))
        .addMethod(
          getUpdateStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              fr.insa.mesh.grpc.UpdateStatusRequest,
              fr.insa.mesh.grpc.UpdateStatusResponse>(
                service, METHODID_UPDATE_STATUS)))
        .build();
  }

  private static abstract class SampleServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SampleServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return fr.insa.mesh.grpc.Labotrack.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SampleService");
    }
  }

  private static final class SampleServiceFileDescriptorSupplier
      extends SampleServiceBaseDescriptorSupplier {
    SampleServiceFileDescriptorSupplier() {}
  }

  private static final class SampleServiceMethodDescriptorSupplier
      extends SampleServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    SampleServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SampleServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SampleServiceFileDescriptorSupplier())
              .addMethod(getGetSampleMethod())
              .addMethod(getUpdateStatusMethod())
              .build();
        }
      }
    }
    return result;
  }
}
