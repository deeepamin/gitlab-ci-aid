package com.github.deeepamin.ciaid.model.gitlab;

import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public record Input(String name, String description, String defaultValue, InputType inputType, SmartPsiElementPointer<YAMLKeyValue> inputElement) {
}
