package com.devgame.listeners;

import com.devgame.services.AnimationService;
import com.devgame.services.LevelingService;
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

public class TestRunListener extends SMTRunnerEventsAdapter {
    
    @Override
    public void onTestingFinished(@NotNull SMTestProxy.SMRootTestProxy testsRoot) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return;
        
        Project project = projects[0];
        
        int passed = testsRoot.getChildren().stream()
            .filter(test -> !test.isDefect())
            .mapToInt(test -> 1)
            .sum();
        
        int failed = testsRoot.getChildren().stream()
            .filter(SMTestProxy::isDefect)
            .mapToInt(test -> 1)
            .sum();
        
        boolean allPassed = failed == 0 && passed > 0;
        
        LevelingService levelingService = LevelingService.getInstance(project);
        AnimationService animationService = AnimationService.getInstance(project);
        
        if (allPassed) {
            levelingService.onTestPassed(passed);
            animationService.showBossBattle(true);
        } else if (failed > 0) {
            levelingService.onTestFailed(failed);
            levelingService.onTestPassed(passed);
            animationService.showBossBattle(false);
        }
    }
}
