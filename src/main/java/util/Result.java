package util;

import java.util.Optional;

public class Result<TVal, TErr> {
    private final TVal value;
    private final TErr error;

    private Result(TVal value, TErr error) {
        this.value = value;
        this.error = error;
    }

    public static <TVal, TErr> Result<TVal, TErr> ok(TVal value){
        return new Result<>(value, null);
    }

    public static <TVal, TErr> Result<TVal, TErr> err(TErr err){
        return new Result<>(null, err);
    }


    public boolean isOk(){
        return error == null;
    }

    public Optional<TVal> getVal(){
        return Optional.ofNullable(value);
    }

    public Optional<TErr> getErr(){
        return Optional.ofNullable(error);
    }
}
