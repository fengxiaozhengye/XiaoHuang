package com.edusmart.service;

import com.edusmart.common.BusinessException;
import com.edusmart.entity.StudentProfile;
import com.edusmart.entity.User;
import com.edusmart.repository.StudentProfileRepository;
import com.edusmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public StudentProfile getProfile(Long userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }

    public StudentProfile createOrUpdateProfile(Long userId, Map<String, Object> profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElse(new StudentProfile());

        if (profile.getUser() == null) {
            profile.setUser(user);
        }

        if (profileData.containsKey("learningStyle")) {
            profile.setLearningStyle(
                com.edusmart.enums.LearningStyle.valueOf((String) profileData.get("learningStyle")));
        }
        if (profileData.containsKey("knowledgeLevel")) {
            profile.setKnowledgeLevel(toJson(profileData.get("knowledgeLevel")));
        }
        if (profileData.containsKey("weakPoints")) {
            profile.setWeakPoints(toJson(profileData.get("weakPoints")));
        }
        if (profileData.containsKey("strongPoints")) {
            profile.setStrongPoints(toJson(profileData.get("strongPoints")));
        }
        if (profileData.containsKey("preference")) {
            profile.setPreference(toJson(profileData.get("preference")));
        }

        return profileRepository.save(profile);
    }

    public Map<String, Object> getProfileSummary(Long userId) {
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return Map.of("exists", false);
        }
        return Map.of(
                "exists", true,
                "learningStyle", profile.getLearningStyle() != null ? profile.getLearningStyle().name() : "UNKNOWN",
                "knowledgeLevel", profile.getKnowledgeLevel() != null ? profile.getKnowledgeLevel() : "{}",
                "weakPoints", profile.getWeakPoints() != null ? profile.getWeakPoints() : "[]",
                "strongPoints", profile.getStrongPoints() != null ? profile.getStrongPoints() : "[]"
        );
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String s) return s;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
