package org.dcache.services.pinmanager;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.NotSerializableException;

import diskCacheV111.vehicles.Message;

import dmg.cells.nucleus.CellPath;
import dmg.cells.nucleus.CellAdapter;
import dmg.cells.nucleus.CellMessage;
import dmg.cells.nucleus.CellMessageAnswerable;
import dmg.cells.nucleus.NoRouteToCellException;

import statemap.FSMContext;
    
class SMCTask implements CellMessageAnswerable 
{
    private FSMContext _fsm;
    protected CellAdapter _cell;

    public SMCTask(CellAdapter cell) 
    {
        _cell = cell;
    }

    protected void setContext(FSMContext o)
    {
        _fsm = o;
    }

    private void transition(String name, Object ...arguments)
    {
        try {
            Class[] parameterTypes = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++)
                parameterTypes[i] = arguments[i].getClass();
            Method m = _fsm.getClass().getMethod(name, parameterTypes);
            m.invoke(_fsm, arguments);
        } catch (NoSuchMethodException e) {
            // FSM is not interested in the message. No problem.
        } catch (IllegalAccessException e) {
            // We are not allowed to call this method. Better escalate it.
            throw new RuntimeException("Bug detected", e);
        } catch (InvocationTargetException e) {
            // The context is not supposed to throw exceptions, so
            // smells like a bug.
            throw new RuntimeException("Bug detected", e);
        } catch (statemap.TransitionUndefinedException e) {
            throw new RuntimeException("State machine is incomplete", e);
        }
    }

    public void answerArrived(CellMessage question, CellMessage answer) 
    {
        transition("answerArrived", answer.getMessageObject());
    }

    public void answerTimedOut(CellMessage request) 
    {
        transition("timeout");
    }
        
    public void exceptionArrived(CellMessage request, Exception exception) 
    {
        transition("exceptionArrived", exception);
    }

    public void sendMessage(CellPath path, Message message, long timeout)
    {
        try {
            _cell.sendMessage(new CellMessage(path, message),
                              true, true, this, timeout);
        } catch (NotSerializableException e) {
            throw new RuntimeException("Bug detected", e);
        } 
    }

    public void sendMessage(CellPath path, Message message)
        throws NoRouteToCellException
    {
        try {
            _cell.sendMessage(new CellMessage(path, message));
        } catch (NotSerializableException e) {
            throw new RuntimeException("Bug detected", e);
        } 
    }
    
    public String getCellName() {
        return _cell.getNucleus().getCellName();
    }
}

