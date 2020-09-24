package util;

import java.util.Optional;

/**
 * Objects of this class represent some result and contain either successfully computed value or some error
 *
 * @param <TVal> type of expected value
 * @param <TErr> type of possible error
 */
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

    /**
     * Returns true if this result doesn't represent an error
     */
    public boolean isOk(){
        return error == null;
    }

    /**
     * @return Optional containing successfully computed result. May be empty if this result represents an error
     */
    public Optional<TVal> getVal(){
        return Optional.ofNullable(value);
    }

    public Optional<TErr> getErr(){
        return Optional.ofNullable(error);
    }
}
