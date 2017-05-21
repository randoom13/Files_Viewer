package am.android.example.android.filesviewer.finder.validation;

public interface Validatable<K, V extends Object> {
    V isValid(K arg, String searchFilter);
}
