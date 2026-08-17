export type QuestionType = 'text' | 'single-choice' | 'multi-choice' | 'time-range' | 'textarea';

export interface Question {
  key: string;
  type: QuestionType;
  prompt: string;
  subtext?: string;
  options?: string[]; // for single-choice / multi-choice
  optional?: boolean;
}

export const ONBOARDING_QUESTIONS: Question[] = [
  {
    key: 'name',
    type: 'text',
    prompt: "What should we call you?",
  },
  {
    key: 'role',
    type: 'single-choice',
    prompt: "What best describes you right now?",
    options: ['Student', 'Working professional', 'Something else'],
  },
  {
    key: 'field',
    type: 'text',
    prompt: "What field or subject are you in?",
    subtext: "e.g. Computer Science, Marketing, Design",
    optional: true,
  },
  {
    key: 'interests',
    type: 'multi-choice',
    prompt: "What are you into?",
    subtext: "Pick as many as you like",
    options: ['Coding', 'Fitness', 'Music', 'Reading', 'Art', 'Gaming', 'Cooking', 'Writing', 'Travel', 'Sports'],
  },
  {
    key: 'focusAreas',
    type: 'text',
    prompt: "Anything specific you want to focus on?",
    subtext: "e.g. productivity, deep work, mindfulness",
    optional: true,
  },
  {
    key: 'goalsText',
    type: 'textarea',
    prompt: "What's a goal you're working toward?",
    optional: true,
  },
];