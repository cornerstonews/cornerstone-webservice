/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cornerstonews.webservice.exception;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;

public class JsonConstraintViolation<T> implements ConstraintViolation<T> {

    private ConstraintViolation<T> violation;

    @SuppressWarnings("unchecked")
    public JsonConstraintViolation(T rootBean, String message, String path, String invalidValue) {
        violation = ConstraintViolationImpl.forParameterValidation(null, null, null, message, (Class<T>) rootBean.getClass(), rootBean, null, invalidValue, 
                PathImpl.createPathFromString(path), null, null, null);
    }

    @Override
    public String getMessage() {
        return violation.getMessage();
    }

    @Override
    public String getMessageTemplate() {
        return violation.getMessageTemplate();
    }

    @Override
    public T getRootBean() {
        return violation.getRootBean();
    }

    @Override
    public Class<T> getRootBeanClass() {
        return violation.getRootBeanClass();
    }

    @Override
    public Object getLeafBean() {
        return violation.getLeafBean();
    }

    @Override
    public Object[] getExecutableParameters() {
        return violation.getExecutableParameters();
    }

    @Override
    public Object getExecutableReturnValue() {
        return violation.getExecutableReturnValue();
    }

    @Override
    public Path getPropertyPath() {
        return violation.getPropertyPath();
    }

    @Override
    public Object getInvalidValue() {
        return violation.getInvalidValue();
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return violation.getConstraintDescriptor();
    }

    @Override
    public <U> U unwrap(Class<U> type) {
        return violation.unwrap(type);
    }

}
