import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ONBOARDING_QUESTIONS } from './questions';
import client from '../api/client';

type Answers = Record<string, string | string[]>;

export function OnboardingFlow() {
  const [step, setStep] = useState(0);
  const [answers, setAnswers] = useState<Answers>({});
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const question = ONBOARDING_QUESTIONS[step];
  const isLastStep = step === ONBOARDING_QUESTIONS.length - 1;

  function setAnswer(key: string, value: string | string[]) {
    setAnswers((prev) => ({ ...prev, [key]: value }));
  }

  function goNext() {
    if (isLastStep) {
      handleSubmit();
    } else {
      setStep((s) => s + 1);
    }
  }

  function goBack() {
    if (step > 0) setStep((s) => s - 1);
  }

  async function handleSubmit() {
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        name: answers.name ?? '',
        role: (answers.role as string)?.toLowerCase().includes('student')
          ? 'student'
          : (answers.role as string)?.toLowerCase().includes('working')
          ? 'working'
          : 'other',
        field: answers.field ?? null,
        interests: (answers.interests as string[]) ?? [],
        focusAreas: answers.focusAreas ?? null,
        activeHoursStart: '06:00',
        activeHoursEnd: '23:00',
        goalsText: answers.goalsText ?? null,
      };
      await client.put('/api/profile/onboarding', payload);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Something went wrong. Try again?');
      setSubmitting(false);
    }
  }

  const currentValue = answers[question.key];
  const canProceed = question.optional || (
    Array.isArray(currentValue) ? currentValue.length > 0 : !!currentValue
  );

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-slate-900 px-4">
      {/* Progress dots */}
      <div className="mb-8 flex gap-1.5">
        {ONBOARDING_QUESTIONS.map((_, i) => (
          <div
            key={i}
            className={`h-1.5 rounded-full transition-all ${
              i === step ? 'w-6 bg-indigo-500' : i < step ? 'w-1.5 bg-indigo-500/50' : 'w-1.5 bg-slate-700'
            }`}
          />
        ))}
      </div>

      {/* Card */}
      <div className="w-full max-w-sm rounded-2xl bg-slate-800 p-6 shadow-xl">
        <h2 className="text-lg font-semibold text-white">{question.prompt}</h2>
        {question.subtext && (
          <p className="mt-1 text-sm text-slate-400">{question.subtext}</p>
        )}

        <div className="mt-5">
          <QuestionInput
            question={question}
            value={currentValue}
            onChange={(v) => setAnswer(question.key, v)}
            onEnter={canProceed ? goNext : undefined}
          />
        </div>

        {error && <p className="mt-3 text-sm text-red-400">{error}</p>}

        <div className="mt-6 flex items-center justify-between">
          <button
            onClick={goBack}
            disabled={step === 0}
            className="text-sm text-slate-400 disabled:opacity-0"
          >
            ← Back
          </button>

          <button
            onClick={goNext}
            disabled={!canProceed || submitting}
            className="rounded-lg bg-indigo-500 px-5 py-2 text-sm font-medium text-white hover:bg-indigo-600 disabled:opacity-40"
          >
            {submitting ? 'Saving...' : isLastStep ? "Let's go" : question.optional && !currentValue ? 'Skip' : 'Next'}
          </button>
        </div>
      </div>
    </div>
  );
}

function QuestionInput({
  question,
  value,
  onChange,
  onEnter,
}: {
  question: (typeof ONBOARDING_QUESTIONS)[number];
  value: string | string[] | undefined;
  onChange: (v: string | string[]) => void;
  onEnter?: () => void;
}) {
  if (question.type === 'text') {
    return (
      <input
        autoFocus
        type="text"
        value={(value as string) ?? ''}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && onEnter?.()}
        className="w-full rounded-lg bg-slate-700 p-3 text-white placeholder-slate-500"
        placeholder="Type your answer..."
      />
    );
  }

  if (question.type === 'textarea') {
    return (
      <textarea
        autoFocus
        value={(value as string) ?? ''}
        onChange={(e) => onChange(e.target.value)}
        rows={3}
        className="w-full rounded-lg bg-slate-700 p-3 text-white placeholder-slate-500"
        placeholder="Type your answer..."
      />
    );
  }

  if (question.type === 'single-choice') {
    return (
      <div className="space-y-2">
        {question.options?.map((opt) => (
          <button
            key={opt}
            onClick={() => onChange(opt)}
            className={`w-full rounded-lg p-3 text-left text-sm transition ${
              value === opt
                ? 'bg-indigo-500 text-white'
                : 'bg-slate-700 text-slate-200 hover:bg-slate-600'
            }`}
          >
            {opt}
          </button>
        ))}
      </div>
    );
  }

  if (question.type === 'multi-choice') {
    const selected = (value as string[]) ?? [];
    function toggle(opt: string) {
      const lower = opt.toLowerCase();
      const exists = selected.some((s) => s.toLowerCase() === lower);
      onChange(exists ? selected.filter((s) => s.toLowerCase() !== lower) : [...selected, opt]);
    }
    return (
      <div className="flex flex-wrap gap-2">
        {question.options?.map((opt) => {
          const isSelected = selected.some((s) => s.toLowerCase() === opt.toLowerCase());
          return (
            <button
              key={opt}
              onClick={() => toggle(opt)}
              className={`rounded-full px-3.5 py-1.5 text-sm transition ${
                isSelected
                  ? 'bg-indigo-500 text-white'
                  : 'bg-slate-700 text-slate-200 hover:bg-slate-600'
              }`}
            >
              {opt}
            </button>
          );
        })}
      </div>
    );
  }

  return null;
}