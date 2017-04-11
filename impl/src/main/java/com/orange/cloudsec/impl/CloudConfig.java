package com.orange.cloudsec.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by mrousse on 11/04/17.
 */
public class CloudConfig<T extends DataObject> {
    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    protected ArrayList<T> data;
    protected DataBroker dataBroker;

    public CloudConfig(){
    }

    public CloudConfig(DataBroker dataBroker) {
        this.dataBroker = dataBroker;

        this.data = new ArrayList<T>();
    }

    public T create(T obj, InstanceIdentifier<T> iid){
        if(data.contains(obj))
            return null;

        data.add(obj);
        //pushToStore(obj, iid, LogicalDatastoreType.CONFIGURATION);
        return obj;
    }

    public T update(T obj, InstanceIdentifier<T> iid){
        int objId = data.indexOf(obj);
        //pushToStore(obj, iid, LogicalDatastoreType.CONFIGURATION);

        data.set(objId, obj);
        return obj;
    }

    public boolean delete(T obj, InstanceIdentifier iid){
        //deleteFromStore(iid, LogicalDatastoreType.CONFIGURATION);
        data.remove(obj);
        return false;
    }

    public DataObject read(InstanceIdentifier<T> iid){
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<T> dataObjectOptional = null;
            dataObjectOptional = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, iid).get();
            if(dataObjectOptional.isPresent()){
                T dataNode = dataObjectOptional.get();
                for(DataObject obj: data){
                    if(obj.equals(dataNode))
                        return obj;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void dataTreeChanged(DataTreeModification mod){
        InstanceIdentifier iid = mod.getRootPath().getRootIdentifier();
        T dataAfter = (T) mod.getRootNode().getDataAfter();
        T dataBefore = (T) mod.getRootNode().getDataBefore();
        Class targetType = mod.getRootPath().getRootIdentifier().getTargetType();

        DataObject toSFC = convertToSFC(dataAfter, iid, targetType);
        switch (mod.getRootNode().getModificationType()) {
            case WRITE:
                if(mod.getRootNode().getDataBefore() == null){
                    this.create(dataAfter, iid);
                }else{
                    this.update(dataAfter, iid);
                }
                break;
            case DELETE:
                this.delete(dataBefore, iid);
                break;
            case SUBTREE_MODIFIED:
                break;
            default:
                break;
        }
    }

    private void pushToStore(T object, InstanceIdentifier<T> objectIid, LogicalDatastoreType dsType){
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();

        transaction.put(dsType, objectIid, object, true);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = transaction.submit();
        try {
            commitFuture.checkedGet();
            LOG.warn("Transaction success : {}", commitFuture);
        } catch (Exception e) {
            LOG.error("Transaction failed with error {}", e.getMessage());
            transaction.cancel();
        }
    }

    private void deleteFromStore(InstanceIdentifier<T> object, LogicalDatastoreType dsType){
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.delete(dsType, object);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = transaction.submit();

        try {
            commitFuture.checkedGet();
            LOG.warn("Transaction success : {}", commitFuture);
        } catch (Exception e) {
            LOG.error("Transaction failed with error {}", e.getMessage());
            transaction.cancel();
        }
    }

    private DataObject convertToSFC(T obj, InstanceIdentifier<T> iid, Class targetType){
        if(targetType == org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.service.function.registry.ServiceFunction.class){
            LOG.warn("Hey ! Nothing has been converted but now you know I should create a Service Function :)");
            ServiceFunction output = null;
            return output;
        }else if(targetType == org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder.class){
            LOG.warn("Hey ! Nothing has been converted but now you know I should create a Service Function :)");
            ServiceFunctionForwarder output = null;
            return output;
        }
        return null;
    }
}
