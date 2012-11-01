package system;

import api.Shared;




public interface Worker2Space extends java.rmi.Remote
{
    void register(Worker worker) throws java.rmi.RemoteException, InterruptedException;
    boolean setShared(Shared shared) throws java.rmi.RemoteException, InterruptedException;
    Shared getShared() throws java.rmi.RemoteException, CloneNotSupportedException;
}