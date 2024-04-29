package ismaapp.tortosa.glucoseregister.services;

public abstract class BaseService implements IBaseService {

    protected boolean actionSuccess = false;

    @Override
    public boolean isActionSuccess() {
        return actionSuccess;
    }
}
