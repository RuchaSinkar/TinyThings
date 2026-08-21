package com.tinythings.user;

import java.util.List;

public record UpdateProfileRequest(String name, List<String> interests, String field, String goalsText) {}