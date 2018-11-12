---- MODULE transfer ----
EXTENDS Naturals, Sequences, TLC

(* --algorithm transfer
variables
    vectors = [i \in 1..3 |-> <<0, 0, 0>>],
    messages = [i \in 1..3 |-> <<0, 0, 0>>]

define
    NumProcesses == 3
    NextProc1(index) == (index % 3) + 1
    NextProc2(index) == ((index + 1) % 3) + 1
    NextTick(index) == vectors[index][index] + 1
    Max(one, two) == IF one > two THEN one ELSE two
end define;

macro increment(index) begin
    vectors[index][index] := NextTick(index);
end macro;

macro receive(index) begin
    vectors[index] := <<Max(vectors[index][1], messages[index][1]), Max(vectors[index][2], messages[index][2]), Max(vectors[index][3], messages[index][3])>>;
end macro;

procedure send(sender)
variables receivers = {NextProc1(self), NextProc2(self)};
begin Q:
    with r \in receivers do
        messages[r] := <<Max(vectors[sender][1], messages[r][1]), Max(vectors[sender][2], messages[r][2]), Max(vectors[sender][3], messages[r][3])>>;
    end with;
    return;
end procedure;

fair process Update \in 1..NumProcesses
variables counter = 0;
begin P:
    while counter < 2 do
        A: receive(self);
        B: either
                increment(self);
            or
                increment(self);
                call send(self);
            end either;
        F: counter := counter + 1;
    end while;
end process

end algorithm *)
\* BEGIN TRANSLATION
CONSTANT defaultInitValue
VARIABLES vectors, messages, pc, stack

(* define statement *)
NumProcesses == 3
NextProc1(index) == (index % 3) + 1
NextProc2(index) == ((index + 1) % 3) + 1
NextTick(index) == vectors[index][index] + 1
Max(one, two) == IF one > two THEN one ELSE two

VARIABLES sender, receivers, counter

vars == << vectors, messages, pc, stack, sender, receivers, counter >>

ProcSet == (1..NumProcesses)

Init == (* Global variables *)
        /\ vectors = [i \in 1..3 |-> <<0, 0, 0>>]
        /\ messages = [i \in 1..3 |-> <<0, 0, 0>>]
        (* Procedure send *)
        /\ sender = [ self \in ProcSet |-> defaultInitValue]
        /\ receivers = [ self \in ProcSet |-> {NextProc1(self), NextProc2(self)}]
        (* Process Update *)
        /\ counter = [self \in 1..NumProcesses |-> 0]
        /\ stack = [self \in ProcSet |-> << >>]
        /\ pc = [self \in ProcSet |-> "P"]

Q(self) == /\ pc[self] = "Q"
           /\ \E r \in receivers[self]:
                messages' = [messages EXCEPT ![r] = <<Max(vectors[sender[self]][1], messages[r][1]), Max(vectors[sender[self]][2], messages[r][2]), Max(vectors[sender[self]][3], messages[r][3])>>]
           /\ pc' = [pc EXCEPT ![self] = Head(stack[self]).pc]
           /\ receivers' = [receivers EXCEPT ![self] = Head(stack[self]).receivers]
           /\ sender' = [sender EXCEPT ![self] = Head(stack[self]).sender]
           /\ stack' = [stack EXCEPT ![self] = Tail(stack[self])]
           /\ UNCHANGED << vectors, counter >>

send(self) == Q(self)

P(self) == /\ pc[self] = "P"
           /\ IF counter[self] < 2
                 THEN /\ pc' = [pc EXCEPT ![self] = "A"]
                 ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
           /\ UNCHANGED << vectors, messages, stack, sender, receivers, 
                           counter >>

A(self) == /\ pc[self] = "A"
           /\ vectors' = [vectors EXCEPT ![self] = <<Max(vectors[self][1], messages[self][1]), Max(vectors[self][2], messages[self][2]), Max(vectors[self][3], messages[self][3])>>]
           /\ pc' = [pc EXCEPT ![self] = "B"]
           /\ UNCHANGED << messages, stack, sender, receivers, counter >>

B(self) == /\ pc[self] = "B"
           /\ \/ /\ vectors' = [vectors EXCEPT ![self][self] = NextTick(self)]
                 /\ pc' = [pc EXCEPT ![self] = "F"]
                 /\ UNCHANGED <<stack, sender, receivers>>
              \/ /\ vectors' = [vectors EXCEPT ![self][self] = NextTick(self)]
                 /\ /\ sender' = [sender EXCEPT ![self] = self]
                    /\ stack' = [stack EXCEPT ![self] = << [ procedure |->  "send",
                                                             pc        |->  "F",
                                                             receivers |->  receivers[self],
                                                             sender    |->  sender[self] ] >>
                                                         \o stack[self]]
                 /\ receivers' = [receivers EXCEPT ![self] = {NextProc1(self), NextProc2(self)}]
                 /\ pc' = [pc EXCEPT ![self] = "Q"]
           /\ UNCHANGED << messages, counter >>

F(self) == /\ pc[self] = "F"
           /\ counter' = [counter EXCEPT ![self] = counter[self] + 1]
           /\ pc' = [pc EXCEPT ![self] = "P"]
           /\ UNCHANGED << vectors, messages, stack, sender, receivers >>

Update(self) == P(self) \/ A(self) \/ B(self) \/ F(self)

Next == (\E self \in ProcSet: send(self))
           \/ (\E self \in 1..NumProcesses: Update(self))
           \/ (* Disjunct to prevent deadlock on termination *)
              ((\A self \in ProcSet: pc[self] = "Done") /\ UNCHANGED vars)

Spec == /\ Init /\ [][Next]_vars
        /\ \A self \in 1..NumProcesses : WF_vars(Update(self)) /\ WF_vars(send(self))

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION

OwnerHasHighestSelfVector == [](\A self \in 1..NumProcesses: \A other \in 1..Len(vectors): vectors[self][self] >= vectors[other][self])
PartialOrderingProperty == [](\A i \in 1..NumProcesses:
                                                    \A x \in 1..NumProcesses:
                                                        \A y \in 1..NumProcesses:
                                                            vectors[i][x] < vectors[i][y] ~>
                                                                \A z \in 1..NumProcesses:
                                                                    vectors[z][x] <= vectors[z][y])

====
